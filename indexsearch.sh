#!/bin/bash
echo "indexing..."
java -jar index.jar
echo "index finish..."
java -jar search.jar
