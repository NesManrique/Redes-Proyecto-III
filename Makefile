#JFLAGS = --classpath=/net/raquella/ldc/redes/nanoxml/java/nanoxml-lite-2.2.3.jar
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $<

CLASSES = Certf.class clicert.class buscert.class servcert.class busThread.class servThread.class

all: $(CLASSES)

clean:
	/bin/rm clicert.class buscert.class servcert.class busThread.class servThread.class Certf.class
