@rem set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_251\jre
@rem set JAVA_HOME=C:\Users\Chris Ioannou\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_16.0.2.v20210721-1149\jre

set JAVA_HOME=C:\Program Files\Java\jdk-17.0.2

@rem mvnw install:install-file -Dfile="C:\Program Files\stanford-corenlp-full-3.9.1\stanford-corenlp-3.9.1.jar" -DgroupId=edu.stanford.nlp -DartifactId=corenlp -Dversion=3.9.1 -Dpackaging=jar -DgeneratePom=true

@rem mvnw install:install-file -Dfile="C:\Program Files\stanford-corenlp-full-3.9.1\stanford-corenlp-3.9.1-models.jar" -DgroupId=edu.stanford.nlp -DartifactId=corenlp-models -Dversion=3.9.1 -Dpackaging=jar -DgeneratePom=true

@rem mvnw install:install-file -Dfile="C:\git\repository\NESTOR Project Workspace\lib\prudensApp.jar" -DgroupId=coaching -DartifactId=prudensApp -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

mvnw install:install-file -Dfile="C:\git\repository\NESTOR Project Workspace - Prudens\lib\NLRGLib.jar" -DgroupId=cy.ac.ouc.cognition.nlrg.lib -DartifactId=nlrglib -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
