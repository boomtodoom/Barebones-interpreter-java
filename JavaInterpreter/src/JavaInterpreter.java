import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * JavaInterpreter - a java code designed to take a program written in the barebones language and interpret it.
 * @author Dylan Maeers
 */
public class JavaInterpreter {
    static JavaInterpreter interp = null; //creates a JavaInterpreter object
    private ArrayList<String> variables = new ArrayList<>(); //Creates an arraylist to store all the variables
    private ArrayList<Integer> variableVal = new ArrayList<>(); //Creates an arraylist to store all the variables values
    private ArrayList<String> codeLines= new ArrayList<>(); //Creates an arraylist that contains every line within the code
    private Stack<Integer> stk = new Stack<>(); //creates a stack that contains the return addresses for the while loops
    private int pointer = 0; //Integer that contains the pointer to know which line the interpreter is currently running
    /**
     * The main method that starts by asking the user for the file location of their program and then
     * runs the readFile method to open the file and begins interpreting the program.
     * @param args
     */
    public static void main(String[] args){
        interp = new JavaInterpreter();
        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter the location of your file: ");
        String fileLoc = scan.nextLine();
        interp.readFile(fileLoc);
    }

    /**
     * Takes a string containing the file location as an input and then reads the text file in this location.
     * Contains validation to check if the file location provided ends with .txt and if not then it exits
     * with an error message
     * @param fileLoc
     */
    void readFile(String fileLoc){
        if(fileLoc.endsWith(".txt")){ //checks if the file ends with .txt and if not sends and error message
            File codeFile = new File(fileLoc); //creates a file object using the fileLoc string
            Scanner fileReader = null; //Instantiates a Scanner that is used to read the file
            try {
                fileReader = new Scanner(codeFile);
            } catch (FileNotFoundException e) {
                System.err.println("File not found!"); //Outputs error message if the file can't be found
            }
            while(fileReader.hasNextLine()){
                String line = fileReader.nextLine();
                List<String> codeList= new ArrayList<>();
                if(line.trim().isEmpty()==false) {
                    codeList = Arrays.asList(line.split(";")); //Splits the lines at the semi-colon
                } else {
                    continue;
                }
                if(codeList.size()>0) {
                    codeLines.addAll(codeList);
                } else {
                    codeLines.add(line);
                }
            }
            while(pointer<codeLines.size()){ //runs until every line in the code has been interpreted and ran
                String step = codeLines.get(pointer);
                if(step.contains("clear")){
                    interp.clear();
                }else if(step.contains("incr")){
                    interp.incr();
                } else if (step.contains("decr")) {
                    interp.decr();
                } else if (step.contains("while")){
                    interp.wLoop();
                } else if (step.contains("func")){
                    interp.func();
                } else if (step.contains("end")){
                    System.out.println("End" + stk.peek());
                    if(stk.isEmpty()==false) {
                        pointer = stk.pop();
                    }
                } else if(step.contains("def")){
                    interp.def();
                } else {
                    System.err.println("Not a code statement");

                }
                pointer++;
            }
            for(int i=0;i<variables.size();i++){
                System.out.println("Variable: "+variables.get(i)+" Value: "+variableVal.get(i));
            }
        } else {
            System.err.println("The file location doesnt end with .txt so it isn't a valid filetype");
            System.exit(1);
        }

    }

    /**
     * Method that takes a variable and increases its value by 1, it also checks to see if the provided variable exists
     * and if not the variable is created and given the value of 1.
     */
    void incr(){
        String line = codeLines.get(pointer);
        String[] elements = line.split("\s*\s");
        String variable = elements[1];
        if(variables.contains(variable)==false){
            variables.add(variable);
            variableVal.add(1);
        } else {
            int varIndex = variables.indexOf(variable);
            variableVal.set(varIndex,variableVal.get(varIndex)+1);
        }
        System.out.println(variable + ": " + variableVal.get(variables.indexOf(variable)));
    }

    /**
     * Method that decreases the value of a variable, if the variable does not exist the variable is created
     * and given the value of -1.
     */
    void decr(){
        String line = codeLines.get(pointer);
        String[] elements = line.split("\s*\s");
        String varName = elements[1];
        if(variables.contains(varName)==false){
            variables.add(varName);
            variableVal.add(-1);
        } else {
            int varIndex = variables.indexOf(varName);
            variableVal.set(varIndex,variableVal.get(varIndex)-1);
        }
        System.out.println("Code line "+codeLines.get(pointer));
        System.out.println(varName + ": " + variableVal.get(variables.indexOf(varName)));
    }

    /**
     * Method that sets a variables value to 0, or if the variable doesn't exist it is created and given
     * the value of 0.
     */
    void clear(){
        String line = codeLines.get(pointer);
        String[] elements = line.split("\s*\s");
        String varName = elements[1];
        if(variables.contains(varName)==false){
            variables.add(varName);
            variableVal.add(0);
        } else {
            int varIndex = variables.indexOf(varName);
            variableVal.set(varIndex,0);
        }
        System.out.println(varName + ": " + variableVal.get(variables.indexOf(varName)));
    }

    /**
     * Method that sets up the requirements for a while loop within the language, it runs until the given variable
     * has the value of 0. The while loops return address is stored in a stack so that embedded while loops are
     * possible.
     */
    void wLoop(){
        String line = codeLines.get(pointer);
        String[] element = line.split("\s*\s");
        System.out.println(element[0]+element[1]+element[2]+element[3]+element[4]);
        if(element[2].equals("not")&&element[3].equals("0")&&element[4].equals("do")){ //checks the syntax of the language
            String loopVar = element[1];
            if(variables.contains(loopVar)){
                int value = variableVal.get(variables.indexOf(loopVar));
                if(value==0){
                    int endIndex=-1;
                    for(int i = pointer;i<codeLines.size();i++){
                        if(codeLines.get(i).contains("end")){
                            endIndex = i;
                            break;
                        }
                    }

                    if(endIndex!=-1){
                        pointer = endIndex;
                    } else {
                        System.err.println("There is no end index for your while loop starting at index " + pointer);
                    }
                } else {
                    stk.push(pointer-1); //adds the loop return address to the top of a stack
                }
            }else{
                variables.add(loopVar);
                variableVal.add(0);
                int endIndex=-1;
                for(int i = pointer;i<codeLines.size();i++){
                    if(codeLines.get(i).equals("end")){
                        endIndex = i;
                        break;
                    }
                }

                if(endIndex!=-1){
                    pointer = endIndex;
                } else {
                    System.err.println("There is no end index for your while loop starting at index "+pointer);
                }
            }
        } else {
            System.err.println("Invalid syntax on line "+pointer);
            System.exit(1);
        }

    }

    void def(){
        int endIndex=-1;
        for(int i = pointer;i<codeLines.size();i++){
            if(codeLines.get(i).contains("end")){
                endIndex = i;
                break;
            }
        }

        if(endIndex!=-1){
            pointer = endIndex;
        } else {
            System.err.println("There is no end index for your while loop starting at index " + pointer);
        }
    }

    void func(){
        System.out.println("func");
        stk.push(pointer);
        String line = codeLines.get(pointer);
        String elements[] = line.split("\s*\s");
        if(elements.length>1 && elements[0].equals("func")){
            String funcName = elements[1];
            for(int i=0; i< codeLines.size(); i++){
                if(codeLines.get(i).contains("def")&&codeLines.get(i).contains(elements[1])){
                    pointer = i;
                }
            }
        }
    }
}
