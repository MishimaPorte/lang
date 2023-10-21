# vim: set ft=make :
guru FILE:
    javac -d bin/guru src/guru/*.java
    java -cp bin/guru guru.Guru {{FILE}}

repl:
    javac -d bin/guru src/guru/*.java
    java -cp bin/guru guru.Guru

ast: build_tool
    java -cp bin/guru tool.GenerateAst src/guru

build: bin ast
    javac -d bin/guru src/guru/*.java

build_tool: bin
    javac -d bin/guru src/tool/*.java

clean:
    @rm -rf bin

bin:
    @mkdir -p bin/guru
