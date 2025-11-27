package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Michael Hart
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(-1);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                validateDir();
                Repository.add(args[1]);
                break;
            case "commit":
                if(args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                validateNumArgs(args, 2);
                validateDir();
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                validateDir();
                Repository.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                validateDir();
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                validateDir();
                Repository.global_log();
                break;
            case "find":
                validateNumArgs(args, 2);
                validateDir();
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                validateDir();
                Repository.status();
                break;
            case "checkout":
                if(args.length != 2 && args.length != 3 && args.length != 4) {
                    Utils.exitWithError("Incorrect operands.");
                }
                validateDir();
                Repository.checkout(args);
                break;
            case "branch":
                validateNumArgs(args, 2);
                validateDir();
                Repository.branch(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

     /**
     * Checks the number of arguments versus the expected number,
     * Exit if they do not match.
     * 
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
     public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(-1);
        }
    }

    public static void validateDir() {
        if(!Repository.initialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(-1);
        }
    }
}
