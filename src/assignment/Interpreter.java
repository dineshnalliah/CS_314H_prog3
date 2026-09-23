package assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for loading critter species from text files and interpreting the
 * simple Critter language.
 * 
 * For more information on the purpose of the below two methods, see the
 * included API/ folder and the project description.
 */
public class Interpreter implements CritterInterpreter {

	private final int INVALID = Integer.MIN_VALUE; // garbage value for parsers to know invalid input

	public void executeCritter(Critter c) {
		if (c == null) {
			System.err.println("critter is null");
			return;
		}

		List<Argument> code = c.getCode();
		if (code == null || code.isEmpty()) {
			System.err.println("critter arguments are null or non existent");
			return;
		}

		int line = c.getNextCodeLine();
		int actions = 0;

		while (true) {
			// running off either end of the program means this critter is done
			if (line < 1 || line > code.size()) {
				return;
			}

			actions++;

			// prevent infinite behavior
			final int MAX_ACTIONS = 1000; // 1000 is arbitrary 
			if (actions > MAX_ACTIONS) {
				System.err.println("critter looped without acting; forfeiting its turn");
				return;
			}

			Argument a = code.get(line - 1);
			int p1 = a.getParamOne();
			int p2 = a.getParamTwo();

			switch (a.getInstruction()) {
				case HOP:
					c.hop();
					c.setNextCodeLine(line + 1);
					return;
				case LEFT:
					c.left();
					c.setNextCodeLine(line + 1);
					return;
				case RIGHT:
					c.right();
					c.setNextCodeLine(line + 1);
					return;
				case EAT:
					c.eat();
					c.setNextCodeLine(line + 1);
					return;
				case INFECT:
					c.infect(p1);
					c.setNextCodeLine(line + 1);
					return;

				case GO:
					line = check(true, a, line, c);
					break;
				case IFRANDOM:
					line = check(c.ifRandom(), a, line, c);
					break;
				case IFHUNGRY:
					line = check(c.getHungerLevel() != Critter.HungerLevel.SATISFIED, a, line, c);
					break;
				case IFSTARVING:
					line = check(c.getHungerLevel() == Critter.HungerLevel.STARVING, a, line, c);
					break;
				case IFEMPTY:
					line = check(c.getCellContent(p1) == Critter.EMPTY, a, line, c);
					break;
				case IFALLY:
					line = check(c.getCellContent(p1) == Critter.ALLY, a, line, c);
					break;
				case IFENEMY:
					line = check(c.getCellContent(p1) == Critter.ENEMY, a, line, c);
					break;
				case IFWALL:
					line = check(c.getCellContent(p1) == Critter.WALL, a, line, c);
					break;
				case IFANGLE:
					line = check(c.getOffAngle(p1) == p2, a, line, c);
					break;

				case WRITE:
					c.setReg(p1, p2);
					line++;
					break;
				case ADD:
					c.setReg(p1, c.getReg(p1) + c.getReg(p2));
					line++;
					break;
				case SUB:
					c.setReg(p1, c.getReg(p1) - c.getReg(p2));
					line++;
					break;
				case INC:
					c.setReg(p1, c.getReg(p1) + 1);
					line++;
					break;
				case DEC:
					c.setReg(p1, c.getReg(p1) - 1);
					line++;
					break;
				case IFLT:
					line = check(c.getReg(p1) < c.getReg(p2), a, line, c);
					break;
				case IFEQ:
					line = check(c.getReg(p1) == c.getReg(p2), a, line, c);
					break;
				case IFGT:
					line = check(c.getReg(p1) > c.getReg(p2), a, line, c);
					break;

				default:
					return;
			}
		}
	}

	// clarifies if the run condition is valid and ensures the correct target
	private int check(boolean condition, Argument a, int line, Critter c) {
		if (!condition) {
			return line + 1;
		}
		Argument.Target t = a.getTarget();
		return t.getTrueLine(line, c);
	}

	public CritterSpecies loadSpecies(String filename) throws IOException {
		if (filename == null) {
			System.err.println("loadSpecies: no file name given");
			return null;
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));

			// reading the name
			String name = in.readLine();
			if (name == null) {
				System.err.println(filename + " is empty");
				return null;
			}
			name = name.trim();
			if (name.isEmpty()) {
				System.err.println(filename + " no species name provided");
				return null;
			}

			// reading the instructions, one per line, until a blank line ends the program
			List<Argument> code = new ArrayList<Argument>();
			int lineNum = 1;
			String line = in.readLine();

			while (line != null) {
				lineNum++;
				line = line.trim();

				// a blank line ends the program; everything past it is a comment
				if (line.isEmpty()) {
					break;
				}

				Argument arg = parseArgument(line, filename, lineNum);
				if (arg == null) {
					return null; // no need to print to err since parseArgument accounts for it
				}
				code.add(arg);

				line = in.readLine();
			}

			if (code.isEmpty()) {
				System.err.println(filename + " species '" + name + "' has no arguments");
				return null;
			}

			return new CritterSpecies(name, code);
		}
		catch (IOException e) {
			System.err.println("could not read " + filename + ": " + e.getMessage());
			return null;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					System.err.println("could not close " + filename);
				}
			}
		}
	}

	private enum ParamFormat {
		NONE, TARGET, OP_TARGET, BE_TARGET, BA_TARGET, REG, REG_VAL, REG_REG, REG_REG_TARGET
		// OP - optional, BE - bearing, BA - bearing angle, REG - register, VAL - value
	}

	private static ParamFormat getParamFormat(Argument.Instruction inst) {
		// mapping instructions to what inputs they take in 
		switch (inst) {
            case HOP:
            case LEFT:
            case RIGHT:
            case EAT:
                return ParamFormat.NONE;
            case INFECT:
                return ParamFormat.OP_TARGET;
            case GO:
            case IFRANDOM:
            case IFHUNGRY:
            case IFSTARVING:
                return ParamFormat.TARGET;
            case IFEMPTY:
            case IFALLY:
            case IFENEMY:
            case IFWALL:
                return ParamFormat.BE_TARGET;
            case IFANGLE:
                return ParamFormat.BA_TARGET;
            case INC:
            case DEC:
                return ParamFormat.REG;
            case WRITE:
                return ParamFormat.REG_VAL;
            case ADD:
            case SUB:
                return ParamFormat.REG_REG;
            case IFLT:
            case IFEQ:
            case IFGT:
                return ParamFormat.REG_REG_TARGET;
            default:
                return null;
        }
	}

	private Argument parseArgument(String line, String filename, int lineNum) throws IOException {
		// lowercase so instructions and register names are case-insensitive
		String[] tokens = line.toLowerCase().split("\\s+");

		Argument.Instruction inst = lookUpInstruction(tokens[0]);
		if (inst == null) {
			System.err.println(filename + " " + lineNum + " instruction not defined, got '" + tokens[0] + "'");
			return null;
		}

		ParamFormat format = getParamFormat(inst);
			if (format == null) {
				System.err.println(filename + " " + lineNum + " no parameter format for " + inst);
				return null;
			}

			int pCount = tokens.length - 1;
			int arg1 = 0;
			int arg2 = 0;
			Argument.Target target = null;

			switch (format) {

				case NONE:
					if (!checkParamCount(pCount, 0, filename, lineNum)) return null;
					break;

				case OP_TARGET:
					// infect's n must be a plain line number
					if (pCount == 0) {
						arg1 = 1; // starts at its first instruction
						break;
					}
					if (!checkParamCount(pCount, 1, filename, lineNum)) return null;
					arg1 = parseLineNumber(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					break;

				case TARGET:
					if (!checkParamCount(pCount, 1, filename, lineNum)) return null;
					target = parseTarget(tokens[1], filename, lineNum);
					if (target == null) return null;
					break;

				case BE_TARGET:
					if (!checkParamCount(pCount, 2, filename, lineNum)) return null;
					arg1 = parseBearing(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					target = parseTarget(tokens[2], filename, lineNum);
					if (target == null) return null;
					break;

				case BA_TARGET:
					if (!checkParamCount(pCount, 3, filename, lineNum)) return null;
					arg1 = parseBearing(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					arg2 = parseBearing(tokens[2], filename, lineNum);
					if (arg2 == INVALID) return null;
					target = parseTarget(tokens[3], filename, lineNum);
					if (target == null) return null;
					break;

				case REG:
					if (!checkParamCount(pCount, 1, filename, lineNum)) return null;
					arg1 = parseRegister(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					break;

				case REG_VAL:
					if (!checkParamCount(pCount, 2, filename, lineNum)) return null;
					arg1 = parseRegister(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					arg2 = parseValue(tokens[2], filename, lineNum);
					if (arg2 == INVALID) return null;
					break;

				case REG_REG:
					if (!checkParamCount(pCount, 2, filename, lineNum)) return null;
					arg1 = parseRegister(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					arg2 = parseRegister(tokens[2], filename, lineNum);
					if (arg2 == INVALID) return null;
					break;

				case REG_REG_TARGET:
					if (!checkParamCount(pCount, 3, filename, lineNum)) return null;
					arg1 = parseRegister(tokens[1], filename, lineNum);
					if (arg1 == INVALID) return null;
					arg2 = parseRegister(tokens[2], filename, lineNum);
					if (arg2 == INVALID) return null;
					target = parseTarget(tokens[3], filename, lineNum);
					if (target == null) return null;
					break;

				default:
					System.err.println(filename + " " + lineNum + " unhandled format " + format);
					return null;
			}

		return new Argument(inst, arg1, arg2, target);
	}

	private Argument.Instruction lookUpInstruction(String keyword) {
		for (Argument.Instruction inst : Argument.Instruction.values()) {
			if (inst.name().toLowerCase().equals(keyword)) {
				return inst;
			}
		}
		return null;
	}

	private boolean checkParamCount(int current, int expected, String filename, int lineNum) {
		if (current != expected)  {
			System.err.println("incorrect arg count in file " + filename + " on " + lineNum);
			return false;
		}
		return true;
	}

	private Argument.Target parseTarget(String s, String file, int lineNum) {
        char first = s.charAt(0);
 
        if (first == 'r') {
            int reg = parseRegister(s, file, lineNum);
            if (reg == INVALID) {
                return null;
            }
            return new Argument.Target(Argument.TargetType.REGISTER, reg);
        }
 
        if (first == '+' || first == '-') {
            if (!isInteger(s)) {
                System.err.println(file + " " + lineNum + " impossible jump, got '" + s + "'");
                return null;
            }
            return new Argument.Target(Argument.TargetType.RELATIVE, Integer.parseInt(s));
        }
 
        int line = parseLineNumber(s, file, lineNum);
        if (line == INVALID) {
            return null;
        }
        return new Argument.Target(Argument.TargetType.ABSOLUTE, line);
    }
 
    private int parseLineNumber(String s, String file, int lineNum) {
        if (!isInteger(s)) {
            System.err.println(file + " " + lineNum + " expected a line number, got '" + s + "'");
            return INVALID;
        }
        if (s.charAt(0) == '+' || s.charAt(0) == '-') {
            System.err.println(file + " " + lineNum + " shouldn't be signed, got '" + s + "'");
            return INVALID;
        }
        int n = Integer.parseInt(s);
        if (n < 1) {
            System.err.println(file + " " + lineNum + " line number starts at 1, got '" + s + "'");
            return INVALID;
        }
        return n;
    }

	private int parseBearing(String s, String file, int lineNum) {
        if (!isInteger(s)) {
            System.err.println(file + " " + lineNum + " expected a bearing, got '" + s + "'");
            return INVALID;
        }
        int b = Integer.parseInt(s);
        if (b < 0 || b > 315) {
            System.err.println(file + " " + lineNum + " bearing should be between 0 and 315, got '" + s + "'");
            return INVALID;
        }
        if (b % 45 != 0) {
            System.err.println(file + " " + lineNum + " bearing should be a multiple of 45, got '" + s + "'");
            return INVALID;
        }
        return b;
    }
 
    private int parseRegister(String s, String file, int lineNum) {
        if (s.charAt(0) != 'r') {
            System.err.println(file + " " + lineNum + " expected a register, got '" + s + "'");
            return INVALID;
        }
        String digits = s.substring(1);
        if (digits.isEmpty()) {
            System.err.println(file + " " + lineNum + " register has no number, got '" + s + "'");
            return INVALID;
        }
        if (!isUnsignedInteger(digits)) {
            System.err.println(file + " " + lineNum + " faulty register name, got '" + s + "'");
            return INVALID;
        }
        int r = Integer.parseInt(digits);
        if (r < 1 || r > 10) {
            System.err.println(file + " " + lineNum + " register number should be between 1 and 10, got '" + s + "'");
            return INVALID;
        }
        return r;
    }
 
    private int parseValue(String s, String file, int lineNum) {
        if (!isInteger(s)) {
            System.err.println(file + " " + lineNum + " expected integer, got '" + s + "'");
            return INVALID;
        }
        return Integer.parseInt(s);
    }

	private static boolean isInteger(String s) {
        if (s.isEmpty()) {
            return false;
        }
        char first = s.charAt(0);
        if (first == '+' || first == '-') {
            return isUnsignedInteger(s.substring(1));
        }
        return isUnsignedInteger(s);
    }
 
    // ensures the token is between 1 and 9 digits
    private static boolean isUnsignedInteger(String s) {
        if (s.isEmpty() || s.length() > 9) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

// object to store and format each line argument from the behavior file
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
	static class Target {
		
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
