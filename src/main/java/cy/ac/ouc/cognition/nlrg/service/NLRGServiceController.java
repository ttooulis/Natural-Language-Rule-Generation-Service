package cy.ac.ouc.cognition.nlrg.service;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import cy.ac.ouc.cognition.nlrg.lib.*;

@CrossOrigin
@RestController
@Tag(name="NESTOR Service", description="Knowledge-Based Natural Language to Symbolic Form Translator")
public class NLRGServiceController {

	private boolean init;

	static enum CommandType {
		INIT, LOADMETAKB, GETMETAKBVERSION, GETMETAKBTEXT, GETTITLE, GETSERVERIP, GETSERVERPORT, GETMISC, UNDEFINED;
	}
	
	private static NLRGPipeline	NLRGPipe;
	private static String 		MetaKBVersion = "MKB Not Loaded";
	private static String		MetaKBText = "";
	private static String		ServiceTitle = "NESTOR - Knowledge-Based Natural Language to Symbolic Form Translator";
	private static int			InstanceId = 0;
	private int					ObjectCounter = 0;
	private int					InfoCounter;


	public NLRGServiceController() {

		try {
			NLRGPipe = new NLRGPipeline();
			InstanceId++;
			ObjectCounter++;
			InfoCounter = 0;
		}
		catch (NLRGException nlrge) {
			System.out.println("NESTOR Service failed to initialize NLRG Pipeline Object!: " + nlrge.getMessage());
			nlrge.printStackTrace();			
		}

	}


	@Autowired
	public void setInit(boolean initValue) {
		this.init = initValue;
	}


	private Integer loadMetaKB() {
		try {

			int loadResult = NLRGPipe.loadMetaKnowledgeBase(null);
			
			if (loadResult != 0) {
				MetaKBVersion = NLRGPipe.getMetaKBVersion(null);

				MetaKBText = NLRGPipe.getMetaKnowledgeBaseText();

				System.out.println("Translation Policy Knowledge first line:");
				Integer eolIndex = MetaKBText.indexOf("\n");
				if (eolIndex > -1)
					System.out.println(MetaKBText.substring(0, eolIndex));
				else
					System.out.println(MetaKBText);

				System.out.println("Translation Policy Loaded, Version=[" + MetaKBVersion + "]!");

				return 0;
			}

			else {
				System.out.println("Translation Policy Failed to Load!");
				return 1;
			}

		} catch (Exception | OutOfMemoryError nlpe) {
			System.out.println("Translation Policy failed to Load!");
			nlpe.printStackTrace();

			return 2;
		}
	}


	private ReturnMessage initializeNLRG() {
		try {

			NLRGPipe.loadNLProcessor();
			System.out.println("NLP Loaded!");
			int loadResult = loadMetaKB();
			
			if (loadResult == 0)
				return new ReturnMessage(0, "NESTOR Initialized", "Translation Policy Version=[" + MetaKBVersion + "]!");

			else if (loadResult == 1)
				return new ReturnMessage(1101, "NESTOR Failed to Initialize", "Translation Policy loading error");
			
			else
				return new ReturnMessage(1111, "NESTOR Failed to Initialize", "Translation Policy loading error. Uncaught exception");

		} catch (Exception | OutOfMemoryError nlpe) {
			System.out.println("NESTOR Service failed to initialize!");
			nlpe.printStackTrace();

			return new ReturnMessage(1121, "NESTOR Failed to Initialize", nlpe.getMessage());
		}
	}


	@PostConstruct
	public void postNLRGServiceController() {

		System.out.println("nlrgpipeline.init=[" + Boolean.toString(init) + "]");

		if (init)
			initializeNLRG();

	}


	@Operation(summary  = "NESTOR Service Index Page")
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
	public String index(HttpServletRequest request) {

		return	"<H1>" + ServiceTitle + "</H1><H2>REST Service</H2><H3>Translation Policy Version: " + MetaKBVersion + "</H3>" +
				"[" + request.getLocalName() + "][" + request.getLocalPort()+ "][" + InstanceId + "][" + ObjectCounter + "][" + InfoCounter + "]";

	}


	@Operation(summary  =	"Control NESTOR Service. \n" +
							" 0: Initialize Service, \n" +
							" 1: Load Translation Policy, \n" +
							" 2: Get Translation Policy Version, \n" +
							" 3: Get Translation Policy Text, \n" +
							" 4: Get NESTOR Service Title, \n" +
							" 5: Get NESTOR Service Server Name, \n" +
							" 6: Get NESTOR Service Port, \n" +
							" 7: Get Miscalleneous Information")
    @RequestMapping(value = "/control", method = RequestMethod.GET, produces = "application/json")
	public ReturnMessage controlService(HttpServletRequest request, @RequestParam("command") Integer command) {

		InfoCounter++;

		if (command == CommandType.INIT.ordinal()) {
			return initializeNLRG();	
		}

		else if (command == CommandType.LOADMETAKB.ordinal()) {
			int loadResult = loadMetaKB();
				
			if (loadResult == 0)
				return new ReturnMessage(0, "Translation Policy Loaded", "Version=["+MetaKBVersion+"]");

			else
				return new ReturnMessage(1201, "Translation Policy Failed to Load", "Translation Policy loading error");
		}

		else if (command == CommandType.GETMETAKBVERSION.ordinal()) {

			return new ReturnMessage(0, "Translation Policy Version", MetaKBVersion);
		}

		else if (command == CommandType.GETMETAKBTEXT.ordinal()) {

			return new ReturnMessage(0, "Translation Policy Text", MetaKBText);
		}
		
		else if (command == CommandType.GETTITLE.ordinal()) {

			return new ReturnMessage(0, "NESTOR Service Title", ServiceTitle);
		}
		
		else if (command == CommandType.GETSERVERIP.ordinal()) {

			return new ReturnMessage(0, "NESTOR Service Server Name", request.getLocalName());
		}
		
		else if (command == CommandType.GETSERVERPORT.ordinal()) {

			return new ReturnMessage(0, "NESTOR Service Port", Integer.toString(request.getLocalPort()));
		}
		
		else if (command == CommandType.GETMISC.ordinal()) {

			return new ReturnMessage(0, "NESTOR Service Misc Information",
										ServiceTitle + "," + MetaKBVersion + "," + request.getLocalName() + "," +
										Integer.toString(request.getLocalPort()) + "," + InstanceId + "," + ObjectCounter + "," + InfoCounter);
		}
		
		else {
			
			return new ReturnMessage(1221, "Invalid NESTOR Service Control Command", Integer.toString(command));
		}

	}


    @Operation(summary  = "Process Natural Language Text")
    @RequestMapping(value = "/processnl", method = RequestMethod.POST, produces = "application/json")
	public ReturnMessage processNL(@RequestBody String nlText) {
		try {
			System.out.println("Will try to process NL text: [" + nlText + "]");
			NLRGPipe.processNL(nlText);

			System.out.println("NL Text Processed!");
			return new ReturnMessage(0, "Natural Language Text Processed", NLRGPipe.getParseData());

		} catch (Exception | OutOfMemoryError nlpe) {
			System.out.println("Failed to process NL Text!");
			System.out.println(nlpe.getMessage());
			nlpe.printStackTrace();

			return new ReturnMessage(1301, "Failed to process Natural Language Text", nlpe.getMessage());
		}
	}


    @Operation(summary = "Generate Policy Predicates from Natural Language Text")
    @RequestMapping(value = "/generatemetapredicates", method = RequestMethod.POST, produces = "application/json")
	public ReturnMessage generateMetaPredicates(@RequestBody String nlText) {
		try {
			System.out.println("Will try to generate Policy Predicates from NL text: [" + nlText + "]");
			NLRGPipe.processNL(nlText);
			NLRGPipe.generateMetaPredicates();

			System.out.println("Meta-Predicates generated from NL Text!");
			return new ReturnMessage(0, "Policy Predicate Data", NLRGPipe.getMetaPredicateTextData());

		} catch (Exception nlpe) {
			System.out.println("Failed to generate Policy Predicates!");
			System.out.println(nlpe.getMessage());
			nlpe.printStackTrace();

			return new ReturnMessage(1401, "Failed to generate Policy Predicates", nlpe.getMessage());
		}
	}


    @Operation(summary = "Generate Context from Natural Language Text")
    @RequestMapping(value = "/generatecontext", method = RequestMethod.POST, produces = "application/json")
	public ReturnMessage generateContext(@RequestBody String nlText) {
		try {
			System.out.println("Will try to generate context from NL text: [" + nlText + "]");
			NLRGPipe.processNL(nlText);
			NLRGPipe.generateMetaPredicates();
			NLRGPipe.buildSentencesContext();

			System.out.println("Context generated from NL Text!");
			return new ReturnMessage(0, "Context", NLRGPipe.getSentencesContextTextData());

		} catch (Exception nlpe) {
			System.out.println("Failed to generate context!");
			System.out.println(nlpe.getMessage());
			nlpe.printStackTrace();

			return new ReturnMessage(1501, "Failed to generate context", nlpe.getMessage());
		}
	}


    @Operation(summary = "Generate Rules from Natural Language Text")
    @RequestMapping(value = "/generaterules", method = RequestMethod.POST, produces = "application/json")
	public ReturnMessage generateRules(@RequestBody String nlText) {
		try {
			System.out.println("Will try to generate rules from NL text: [" + nlText + "]");
			NLRGPipe.processNL(nlText);
			NLRGPipe.generateMetaPredicates();
			NLRGPipe.buildSentencesContext();
			NLRGPipe.extractRules(null);

			System.out.println("Rules generated from NL Text!");
			return new ReturnMessage(0, "Rules generated from Natural Language Text", NLRGPipe.buildExtractedRules());

		} catch (Exception | OutOfMemoryError nlpe) {
			System.out.println("Failed to generate rules from NL Text!");
			System.out.println(nlpe.getMessage());
			nlpe.printStackTrace();

			return new ReturnMessage(1601, "Failed to generate rules from Natural Language Text", nlpe.getMessage());
		}
	}


    @Operation(summary = "Generate Rules from Natural Language Text using custom Translation Policy")
    @RequestMapping(value = "/generaterules-customkb", method = RequestMethod.POST, produces = "application/json")
	public ReturnMessage generateRulesPrivate(@RequestBody AdviceData adviceData) {
		String metaKB = "";
		try {
			String nlText = adviceData.getNLText();
			metaKB = adviceData.getMetaKB();

			System.out.println("Will try to generate rules from NL text: [" + nlText + "] using Translation Policy: [" + metaKB + "]");
			NLRGPipe.processNL(nlText);
			NLRGPipe.generateMetaPredicates();
			NLRGPipe.buildSentencesContext();
			NLRGPipe.extractRules(metaKB);

			System.out.println("Rules generated from NL Text and custom Translation Policy!");
			String message = "Rules generated from Natural Language Text";
			if (metaKB != "")
				message += " and custom Translation Policy";
			return new ReturnMessage(0, message, NLRGPipe.buildExtractedRules());

		} catch (Exception | OutOfMemoryError nlpe) {
			System.out.println("Failed to generate rules from NL Text and custom Translation Policy!");
			System.out.println(nlpe.getMessage());
			nlpe.printStackTrace();

			String message = "Failed to generate rules from Natural Language Text";
			if (metaKB != "")
				message += " and custom Translation Policy";
			return new ReturnMessage(1601, message, nlpe.getMessage());
		}
	}


    @Operation(summary = "Generate Rules from Natural Language Text using custom Translation Policy and return Explanation")
    @RequestMapping(value = "/generaterules-customkb-expl", method = RequestMethod.POST, produces = "application/json")
	public ReturnMessageExpl generateRulesPrivateWithExpl(@RequestBody AdviceData adviceData) {
		String metaKB = "";
		try {
			String nlText = adviceData.getNLText();
			metaKB = adviceData.getMetaKB();

			System.out.println("Will try to generate rules from NL text: [" + nlText + "] using Translation Policy: [" + metaKB + "]");
			NLRGPipe.processNL(nlText);
			NLRGPipe.generateMetaPredicates();
			NLRGPipe.buildSentencesContext();
			NLRGPipe.extractRules(metaKB);

			System.out.println("Rules generated from NL Text and custom Translation Policy!");
			String message = "Rules generated from Natural Language Text";
			if (metaKB != "")
				message += " and custom Translation Policy";
			
			String markedRules = NLRGPipe.getMarkedRulesText();
			String markedRulesChain = NLRGPipe.getMarkedRulesChainText();
			if (markedRulesChain.isBlank())
				markedRulesChain = markedRules;
			
			return new ReturnMessageExpl(0, message, NLRGPipe.buildExtractedRules(), markedRules, markedRulesChain);

		} catch (Exception | OutOfMemoryError nlpe) {
			System.out.println("Failed to generate rules from NL Text and custom Translation Policy!");
			System.out.println(nlpe.getMessage());
			nlpe.printStackTrace();

			String message = "Failed to generate rules from Natural Language Text";
			if (metaKB != "")
				message += " and custom Translation Policy";
			return new ReturnMessageExpl(1601, message, nlpe.getMessage(), "", "");
		}
	}

}



@Schema(title = "AdviceData", description = "Advice data that include Natural Language Text and Translation Policy")
class AdviceData {

	private String		NLText = null;
	private String		MetaKB = null;


	
	public AdviceData(int ResultCode, String NLText, String MetaKB) {

		this.NLText = NLText;
		this.MetaKB = MetaKB;
									   
	}



	public AdviceData NLText(String NLText) {
		this.NLText = NLText;
		return this;
	}

    @Schema(title = "NLText", description = "Natural language text to be translated to symbolic form")
	public String getNLText() {
		return this.NLText;
	}
	
	public void setNLText(String NLText) {
		this.NLText = NLText;
	}



	public AdviceData MetaKB(String MetaKB) {
		this.MetaKB = MetaKB;
		return this;
	}

    @Schema(title = "metaKB", description = "Custom Translation Policy")
	public String getMetaKB() {
		return this.MetaKB;
	}
	
	public void setMetaKB(String MetaKB) {
		this.MetaKB = MetaKB;
	}



	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("Advice Data {\n");    
		sb.append("    NLText: ").append(toIndentedString(NLText)).append("\n");
		sb.append("    MetaKB: ").append(toIndentedString(MetaKB)).append("\n");
		sb.append("}");

		return sb.toString();
	}

	
	
	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {

		if (o == null) {
			return "null";
		}

		return o.toString().replace("\n", "\n    ");

	}

}



@Schema(title = "ReturnMessage", description = "Return Message Object")
class ReturnMessage {

	private int		ResultCode = -1;
	private String	Message = null;
	private String	TextData = null;

	
	
	public ReturnMessage(int ResultCode, String Message, String TextData) {

		this.ResultCode = ResultCode;
		this.Message = Message;
		this.TextData = TextData;			   
	}


	
	public ReturnMessage ResultCode(int ResultCode) {
		this.ResultCode = ResultCode;
		return this;
	}

	@Schema(title="ResultCode", required = true, description = "Service internal result code")
	@NotNull
	public int getResultCode() {
		return ResultCode;
	}

	public void setResultCode(int ResultCode) {
		this.ResultCode = ResultCode;
	}
	


	public ReturnMessage Message(String Message) {
		this.Message = Message;
		return this;
	}

	@Schema(title="Message", description = "Service result related message")
	public String getMessage() {
		return Message;
	}

	public void setMessage(String Message) {
		this.Message = Message;
	}



	public ReturnMessage TextData(String TextData) {
		this.TextData = TextData;
		return this;
	}

	@Schema(title="TextData", description = "Result text data")
	public String getTextData() {
		return this.TextData;
	}

	public void setTextData(String TextData) {
		this.TextData = TextData;
	}



	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("Return Message {\n");    
		sb.append("    ResultCode: ").append(toIndentedString(Integer.valueOf(ResultCode))).append("\n");
		sb.append("    Message: ").append(toIndentedString(Message)).append("\n");
		sb.append("    TextData: ").append(toIndentedString(TextData)).append("\n");
		sb.append("}");

		return sb.toString();
	}
	
		
	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {

		if (o == null) {
			return "null";
		}

		return o.toString().replace("\n", "\n    ");

	}

}




@Schema(title = "ReturnMessageWithExplanation", description = "Return Message Object with Explanation")
class ReturnMessageExpl {

	private int		ResultCode = -1;
	private String	Message = null;
	private String	TextData = null;
	private String	MarkedRules = null;
	private String	MarkedRulesChain = null;

	
	
	public ReturnMessageExpl(int ResultCode, String Message, String TextData, String MarkedRules, String MarkedRulesChain) {

		this.ResultCode = ResultCode;
		this.Message = Message;
		this.TextData = TextData;
		this.MarkedRules = MarkedRules;
		this.MarkedRulesChain = MarkedRulesChain;
	}


	
	public ReturnMessageExpl ResultCode(int ResultCode) {
		this.ResultCode = ResultCode;
		return this;
	}

	@Schema(title="ResultCode", required = true, description = "Service internal result code")
	@NotNull
	public int getResultCode() {
		return ResultCode;
	}

	public void setResultCode(int ResultCode) {
		this.ResultCode = ResultCode;
	}
	


	public ReturnMessageExpl Message(String Message) {
		this.Message = Message;
		return this;
	}

	@Schema(title="Message", description = "Service result related message")
	public String getMessage() {
		return Message;
	}

	public void setMessage(String Message) {
		this.Message = Message;
	}



	public ReturnMessageExpl TextData(String TextData) {
		this.TextData = TextData;
		return this;
	}

	@Schema(title="TextData", description = "Result text data")
	public String getTextData() {
		return this.TextData;
	}

	public void setTextData(String TextData) {
		this.TextData = TextData;
	}



	public ReturnMessageExpl MarkedRules(String MarkedRules) {
		this.MarkedRules = MarkedRules;
		return this;
	}

	@Schema(title="MarkedRules", description = "Marked Rules text data")
	public String getMarkedRules() {
		return this.MarkedRules;
	}

	public void setMarkedRules(String MarkedRules) {
		this.MarkedRules = MarkedRules;
	}



	public ReturnMessageExpl MarkedRulesChain(String MarkedRulesChain) {
		this.MarkedRulesChain = MarkedRulesChain;
		return this;
	}

	@Schema(title="MarkedRulesChain", description = "Marked Rules Chain text data")
	public String getMarkedRulesChain() {
		return this.MarkedRulesChain;
	}

	public void setMarkedRulesChain(String MarkedRulesChain) {
		this.MarkedRulesChain = MarkedRulesChain;
	}



	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("Return Message {\n");    
		sb.append("    ResultCode: ").append(toIndentedString(Integer.valueOf(ResultCode))).append("\n");
		sb.append("    Message: ").append(toIndentedString(Message)).append("\n");
		sb.append("    TextData: ").append(toIndentedString(TextData)).append("\n");
		sb.append("    MarkedRules: ").append(toIndentedString(MarkedRules)).append("\n");
		sb.append("    MarkedRulesChain: ").append(toIndentedString(MarkedRulesChain)).append("\n");
		sb.append("}");

		return sb.toString();
	}
	
	
	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {

		if (o == null) {
			return "null";
		}

		return o.toString().replace("\n", "\n    ");

	}
}