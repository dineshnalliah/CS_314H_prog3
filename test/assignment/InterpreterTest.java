package assignment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class InterpreterTest {

    // JUnit uses this for temporary files (reduces clutter)
    @TempDir
    Path tempDirectory;

    private static Interpreter interpreter;

    @BeforeAll
    static void setupAll() {

        interpreter = new Interpreter();
    }


    @BeforeEach
    void setup() {

    }

    @Test
    void loadSpeciesComments() throws IOException {
        CritterSpecies species = loadSpecies(
                "Spinner\n"
                + "right\n"
                + "go 1\n"
                + "\n"
                + "this is a random comment!\n"
                + "and here's another.\n");

        assertNotNull(species);
        assertEquals("Spinner", species.getName());
        assertEquals(2, species.getCode().size());
    }

    @Test
    void invalidBearing() throws IOException {
        CritterSpecies species = loadSpecies(
                "BadBearing\n"
                + "ifempty 20 2\n"
                + "hop\n");

        assertNull(species);
    }

    @Test
    void hopCommand() throws IOException {
        TestCritter critter = createCritter("hop\n");

        interpreter.executeCritter(critter);

        assertEquals("hop", critter.action);
        assertEquals(2, critter.nextCodeLine);
    }

    @Test
    void leftCommand() throws IOException {
        TestCritter critter = createCritter("left\n");

        interpreter.executeCritter(critter);

        assertEquals("left", critter.action);
    }

    @Test
    void rightCommand() throws IOException {
        TestCritter critter = createCritter("right\n");

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void eatCommand() throws IOException {
        TestCritter critter = createCritter("eat\n");

        interpreter.executeCritter(critter);

        assertEquals("eat", critter.action);
    }

    @Test
    void infectCommand() throws IOException {
        TestCritter critter = createCritter("infect 3\n");

        interpreter.executeCritter(critter);

        assertEquals("infect", critter.action);
        assertEquals(3, critter.infectLine);
    }

    @Test
    void goCommand() throws IOException {
        // Line 2 is the fallback action
        // a successful jump should reach line 3
        // The conditional command tests below use a similar idea
        TestCritter critter = createCritter(
                "go 3\n"
                + "left\n"
                + "right\n");

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void relativeJumpCommand() throws IOException {
        TestCritter critter = createCritter(
                "go +2\n"
                + "left\n"
                + "right\n");

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void registerJumpCommand() throws IOException {
        TestCritter critter = createCritter(
                "write r1 4\n"
                + "go r1\n"
                + "left\n"
                + "right\n");

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void invalidJumpDestination() throws IOException {
        TestCritter critter = createCritter("go r1\n");

        interpreter.executeCritter(critter);

        assertNull(critter.action);
        assertEquals(2, critter.nextCodeLine);

        interpreter.executeCritter(critter);

        assertNull(critter.action);
        assertEquals(2, critter.nextCodeLine);
    }

    @Test
    void resumesAcrossTurns() throws IOException {
        TestCritter critter = createCritter(
                "inc r1\n"
                + "hop\n"
                + "inc r1\n"
                + "left\n"
                + "go 1\n");

        interpreter.executeCritter(critter);
        assertEquals("hop", critter.action);
        assertEquals(1, critter.getReg(1));
        assertEquals(3, critter.nextCodeLine);

        interpreter.executeCritter(critter);
        assertEquals("left", critter.action);
        assertEquals(2, critter.getReg(1));
        assertEquals(5, critter.nextCodeLine);

        interpreter.executeCritter(critter);
        assertEquals("hop", critter.action);
        assertEquals(3, critter.getReg(1));
        assertEquals(3, critter.nextCodeLine);
    }

    @Test
    void ifRandomCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifrandom 3\n"
                + "left\n"
                + "right\n");
        critter.randomResult = true;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifHungryCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifhungry 3\n"
                + "left\n"
                + "right\n");
        critter.hungerLevel = Critter.HungerLevel.HUNGRY;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifStarvingCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifstarving 3\n"
                + "left\n"
                + "right\n");
        critter.hungerLevel = Critter.HungerLevel.STARVING;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifEmptyCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifempty 0 3\n"
                + "left\n"
                + "right\n");
        critter.cellContent = Critter.EMPTY;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifAllyCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifally 0 3\n"
                + "left\n"
                + "right\n");
        critter.cellContent = Critter.ALLY;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifEnemyCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifenemy 0 3\n"
                + "left\n"
                + "right\n");
        critter.cellContent = Critter.ENEMY;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifWallCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifwall 0 3\n"
                + "left\n"
                + "right\n");
        critter.cellContent = Critter.WALL;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifAngleCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifangle 0 90 3\n"
                + "left\n"
                + "right\n");
        critter.offAngle = 90;

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void writeCommand() throws IOException {
        TestCritter critter = createCritter(
                "write r1 7\n"
                + "hop\n");

        interpreter.executeCritter(critter);

        assertEquals(7, critter.getReg(1));
        assertEquals("hop", critter.action);
    }

    @Test
    void addCommand() throws IOException {
        TestCritter critter = createCritter(
                "add r1 r2\n"
                + "hop\n");
        critter.setReg(1, 5);
        critter.setReg(2, 3);

        interpreter.executeCritter(critter);

        assertEquals(8, critter.getReg(1));
        assertEquals("hop", critter.action);
    }

    @Test
    void subCommand() throws IOException {
        TestCritter critter = createCritter(
                "sub r1 r2\n"
                + "hop\n");
        critter.setReg(1, 5);
        critter.setReg(2, 3);

        interpreter.executeCritter(critter);

        assertEquals(2, critter.getReg(1));
        assertEquals("hop", critter.action);
    }

    @Test
    void incrCommand() throws IOException {
        TestCritter critter = createCritter(
                "inc r1\n"
                + "hop\n");
        critter.setReg(1, 5);

        interpreter.executeCritter(critter);

        assertEquals(6, critter.getReg(1));
        assertEquals("hop", critter.action);
    }

    @Test
    void decrCommand() throws IOException {
        TestCritter critter = createCritter(
                "dec r1\n"
                + "hop\n");
        critter.setReg(1, 5);

        interpreter.executeCritter(critter);

        assertEquals(4, critter.getReg(1));
        assertEquals("hop", critter.action);
    }

    @Test
    void ifLessCommand() throws IOException {
        TestCritter critter = createCritter(
                "iflt r1 r2 3\n"
                + "left\n"
                + "right\n");
        critter.setReg(1, 2);
        critter.setReg(2, 5);

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifEqualCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifeq r1 r2 3\n"
                + "left\n"
                + "right\n");
        critter.setReg(1, 5);
        critter.setReg(2, 5);

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void ifGreaterCommand() throws IOException {
        TestCritter critter = createCritter(
                "ifgt r1 r2 3\n"
                + "left\n"
                + "right\n");
        critter.setReg(1, 5);
        critter.setReg(2, 2);

        interpreter.executeCritter(critter);

        assertEquals("right", critter.action);
    }

    @Test
    void pastProgramEnd() throws IOException {
        // the critter shouldn't restart its program on the next turn
        CritterSpecies species = loadSpecies(
                "OneLine\n"
                + "write r1 7\n");
        TestCritter critter = new TestCritter(species);

        interpreter.executeCritter(critter);

        assertEquals(7, critter.getReg(1));
        assertEquals(2, critter.nextCodeLine);
        assertNull(critter.action);
    }

    @Test
    void longProgram() throws IOException {
        // Non-action commands can run repeatedly before an action
        StringBuilder program = new StringBuilder("LongProgram\n");
        for (int i = 0; i < 1001; i++) {
            program.append("inc r1\n");
        }
        program.append("hop\n");

        CritterSpecies species = loadSpecies(program.toString());
        TestCritter critter = new TestCritter(species);

        interpreter.executeCritter(critter);

        assertEquals(1001, critter.getReg(1));
        assertEquals("hop", critter.action);
    }

    private TestCritter createCritter(String instructions) throws IOException {
        // most command tests only need a species name followed by a few lines
        CritterSpecies species = loadSpecies("TestSpecies\n" + instructions);
        assertNotNull(species);
        return new TestCritter(species);
    }

    private CritterSpecies loadSpecies(String contents) throws IOException {
        Path file = tempDirectory.resolve("species.cri");
        Files.writeString(file, contents);
        return interpreter.loadSpecies(file.toString());
    }

    // A testable Critter implementation that records the interpreter's action
    private static class TestCritter implements Critter {

        private final List code;
        // Register numbers start at 1, so index 0 is not used intially
        private final int[] registers = new int[Critter.REGISTERS + 1];

        private int nextCodeLine = 1;
        private HungerLevel hungerLevel = HungerLevel.SATISFIED;
        private int cellContent = Critter.EMPTY;
        private int offAngle = Critter.BAD;
        private boolean randomResult;
        private String action;
        private int infectLine = 1;

        TestCritter(CritterSpecies species) {
            code = species.getCode();
        }

        public List getCode() {
            return code;
        }

        public int getNextCodeLine() {
            return nextCodeLine;
        }

        public void setNextCodeLine(int line) {
            nextCodeLine = line;
        }

        public int getReg(int register) {
            return registers[register];
        }

        public void setReg(int register, int value) {
            registers[register] = value;
        }

        public HungerLevel getHungerLevel() {
            return hungerLevel;
        }

        // Rather than running a simulation, these methods simply record the action
        public void hop() {
            action = "hop";
        }

        public void left() {
            action = "left";
        }

        public void right() {
            action = "right";
        }

        public void eat() {
            action = "eat";
        }

        public void infect() {
            action = "infect";
            infectLine = 1;
        }

        public void infect(int line) {
            action = "infect";
            infectLine = line;
        }

        public int getCellContent(int bearing) {
            return cellContent;
        }

        public int getOffAngle(int bearing) {
            return offAngle;
        }

        public boolean ifRandom() {
            return randomResult;
        }
    }
}
