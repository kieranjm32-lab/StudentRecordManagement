#!/bin/bash
cd "$(dirname "$0")"
javac *.java
if [ $? -ne 0 ]; then
  echo "Compile failed. Is the Java JDK installed?"
  exit 1
fi
java StudentApp
