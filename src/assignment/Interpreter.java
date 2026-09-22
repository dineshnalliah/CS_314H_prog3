package assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Responsible for loading critter species from text files and interpreting the
 * simple Critter language.
 * 
 * For more information on the purpose of the below two methods, see the
 * included API/ folder and the project description.
 */
public class Interpreter implements CritterInterpreter {

	public void executeCritter(Critter c) {
		// obviously, your code should do something
		return;
	}

	public CritterSpecies loadSpecies(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("filename"));
		try { 
			// reading the name
			String name = in.readLine();
			if (name == null || name.isEmpty()) {
				in.close();
				throw new IOException("no name provided");
			}
			name = name.trim();

			// reading the instructions



		}
		finally {

		}
		in.close();



		return null;
	}
}

class Argument {

	public enum Instruction {
		// main actions
		HOP, LEFT, RIGHT, EAT, INFECT,
		// control flow
		GO, IFRANDOM, IFHUNGRY, IFSTARVING, IFEMPTY, IFALLY, IFENEMY, IFWALL, IFANGLE,
		// register related
		WRITE, ADD, SUB, INC, DEC, IFLT, IFEQ, IFGT
	}

	public enum TargetType {
		ABSOLUTE, RELATIVE, REGISTER
	}

	// object type to store targets since they differ across arguments
	private static class Target {
		
		private TargetType type;
		private int value;

		public Target(TargetType type, int value) {
			this.type = type;
			this.value = value;
		}

		public TargetType getType() { return type; }
		public int getValue() { return value; }

		// gets the true nth line where code should be executed
		public int getTrueLine(int currentLine, Critter c) {
            switch (type) {
                case ABSOLUTE:
                    return value;
                case RELATIVE:
                    return currentLine + value;
                case REGISTER:
                    return c.getReg(value);
                default:
                    throw new IllegalStateException("unknown type " + type);
            }
        }
	}

	private Instruction inst;
	private int param1;
	private int param2;
	private Target target;

	public Argument(Instruction inst, int param1, int param2, Target target) {
		this.inst = inst;
		this.param1 = param1;
		this.param2 = param2;
		this.target = target;
	}

	public Instruction getInstruction() {
		return inst;
	}

	public int getParamOne() {
		return param1;
	}

	public int getParamTwo() { 
		return param2;
	}

	public Target getTarget() {
		return target;
	}
}
