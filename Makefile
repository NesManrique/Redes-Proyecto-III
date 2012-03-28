#JFLAGS = --classpath=/net/raquella/ldc/redes/nanoxml/java/nanoxml-lite-2.2.3.jar
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $<

CLASSES = Certf.class clicert.class buscert.class servcert.class OperBus.class OperServ.class
RMIC = servcert_Stub.class buscert_Stub.class

all: $(CLASSES) $(RMIC)

$(RMIC): $(CLASSES)
	rmic buscert servcert

clean:
	/bin/rm servcert_Stub.class buscert_Stub.class clicert.class buscert.class servcert.class Certf.class OperBus.class OperServ.class
