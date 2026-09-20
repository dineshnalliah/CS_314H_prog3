# Submission Info

Names:

EIDs:

Emails:

Time Spent:

Collaborators:

Comments:


# Starter Code Info

## Running the code

### VSCode
If you are using VSCode, this project structures should work out of the box.

### IntelliJ
If you are using IntelliJ, it should also work out of the box, but you may need to import the module.

### Other IDEs
If you use a different IDE, you will need to make a Run Configuration with assignment.CritterApplication as the main class

### Command Line
If all else fails and you are forced to use command line:

1. Make sure `javac` and `java` are the same version. You can check by doing `java -version` and 1javac -version`

2. If they are not the same, then download Java 11 (try to get OpenJDK 11, Temurin 11, or Corretto 11)

3. You may then have to change your PATH environment variable to put the new java and javac before the old ones. You can look up how to do this on your operating system.

4. Once you have the same version of java and javac, you can do the following depending on your operating system:

- Windows: Simply run the .bat files as is
```
./run_java.bat  - runs your critter application
./test_java.bat - runs your junit tests
```

- MacOS: You may have to first give your files execute permissions. You can do that with
```
chmod u+x ./run_java.sh ./test_java.sh
```
Then you can do
```
./run_java.sh  - runs your critter application
./test_java.sh - runs your junit tests
```

Let us know if these scripts don't work for you, and we can help you out.