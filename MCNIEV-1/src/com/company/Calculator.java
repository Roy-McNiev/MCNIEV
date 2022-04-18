package com.company;

import com.company.errors.ArgumentError;
import com.company.errors.SyntaxError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class Calculator {
    private static final Stack<Float> digits = new Stack<>();
    private static final Stack<Character> operators = new Stack<>();
    private static final StringBuffer digitTemp = new StringBuffer();

    private static float history = 0;

    private static int getPriority(char c) {
        return switch (c) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            case '(', ')' -> 4;
            default -> -5;
        };
    }

    private static float operate(char operator, float f1, float f2) {
        return switch (operator) {
            case '+' -> f1 + f2;
            case '-' -> f1 - f2;
            case '*' -> f1 * f2;
            case '/' -> f1 / f2;
            case '^' -> (float) Math.pow(new BigDecimal(String.valueOf(f1)).doubleValue(),
                    new BigDecimal(String.valueOf(f2)).doubleValue());
            default -> 0;
        };
    }

    private static String metaExec(String line) throws SyntaxError, ArgumentError {
        /*
         * The final value returned for parenthesis
         */
        float result = 0f;

        /*
         * Finding the last index of right parenthesis in the line
         */
        int lastRightParenthesis = line.lastIndexOf(')');

        /*
         * Counting the left parentheses and right parenthesis, to check if their totals pair and to
         * determine when to execute the content covered by a pair of parenthesis
         */
        int leftParCount, rightParCount = 0;

        /*
         * The number of parameters in the function call, signified by commas
         */
        int parameterNum = 1;

        /*
         * Helps iterate through the parameters being entered in the function
         */
        int iterator;

        /*
         * Stores the parameters for calling the functions
         */
        String[] parameters = new String[2];

        /*
         * Manages function execution
         * Stores the function name for calling future corresponding executions
         */
        String functionDesc = "";

        /*
         * Determining the bound for the execution
         * Prevents getting errors from nesting and parallel parentheses
         */
        for (int i = 0; i < line.length() - 1; i++) {
            if (line.charAt(i) == '(') {
                leftParCount = 1;
                iterator = i + 1;

                if (i == 3) {
                    functionDesc = switch (line.substring(0, i)) {
                        case "SIN" -> "SIN";
                        case "COS" -> "COS";
                        case "TAN" -> "TAN";
                        default -> "";
                    };
                }

                else if (i >= 4) {
                    functionDesc = switch (line.substring(i - 4, i)) {
                        case "COSH" -> "COSH";
                        case "FACT" -> "FACT";
                        case "ROOT" -> "ROOT";
                        case "SINH" -> "SINH";
                        case "SQRT" -> "SQRT";
                        case "TANH" -> "TANH";
                        default -> switch (line.substring(i - 3, i)) {
                            case "SIN" -> "SIN";
                            case "COS" -> "COS";
                            case "TAN" -> "TAN";
                            default -> "";
                        };
                    };
                }

                /*
                 * Iterates the contents in the parenthesis
                 */
                for (int j = i + 1; j < line.length(); j++) {
                    if (line.charAt(j) == '(') leftParCount++;
                    else if (line.charAt(j) == ')') {
                        rightParCount++;
                        if (leftParCount == rightParCount) {
                            lastRightParenthesis = j;
                            parameters[parameterNum - 1] = line.substring(iterator, j);
                            break;
                        }
                    } else if (line.charAt(j) == ',') if (leftParCount - rightParCount == 1) {
                        parameters[parameterNum - 1] = line.substring(i + 1,j);
                        parameterNum++;
                        iterator = j + 1;
                    }
                }

                /*
                 * Uses recursion to manage nesting parentheses
                 */
                try {
                    if (!functionDesc.equals("")) {
                        Class<?> cls = Class.forName("com.company.arithmetics." + functionDesc);
                        if (parameterNum == 1) {
                            Method method = cls.getMethod("solve", Double.class);
                            result = Float.parseFloat(String.valueOf(method.invoke(cls.getDeclaredConstructor().newInstance(),
                                    (double) calculate(metaExec(parameters[0])))));
                        } else if (parameterNum == 2){
                            Method method = cls.getMethod("solve", Double.class, Double.class);
                            result = Float.parseFloat(String.valueOf(method.invoke(cls.getDeclaredConstructor().newInstance(),
                                    (double) calculate(metaExec(parameters[0])),
                                    (double) calculate(metaExec(parameters[1])))));
                        }
                    } else result = calculate(metaExec(line.substring(i + 1, lastRightParenthesis)));

                } catch (StringIndexOutOfBoundsException | ClassNotFoundException |
                        NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    throw new SyntaxError();
                } catch (InvocationTargetException e) {
                    throw new ArgumentError();
                }

                return line.substring(0, i - functionDesc.length()).concat(String.valueOf(result)).concat(
                        line.substring(lastRightParenthesis + 1));
            }
        }
        return line;
    }

    private static void prioritize() {
        for (int i = 0; i < operators.size() - 1; i++) {
            char currentChar = operators.get(i);
            
            
            if (getPriority(currentChar) > getPriority(operators.get(i + 1))) {
                operators.set(i, operators.set(i + 1, currentChar));
                digits.set(i, digits.set(i + 2, digits.set(i + 1, digits.get(i))));
            }
        }
    }

    private static void pushTemp() throws SyntaxError {
        if (!digitTemp.isEmpty()) {
            try {
                if (digitTemp.toString().equals("ANS")) digits.insertElementAt(history, 0);
                else digits.insertElementAt(Float.parseFloat(digitTemp.toString()), 0);
            } catch (NumberFormatException e) {
                throw new SyntaxError();
            } finally {
                digitTemp.setLength(0);
            }
        }
    }

    public static float calculate(String line) throws SyntaxError {
        digits.clear();
        operators.clear();

        float result = 0;
        float f1, f2;

        line = line.replaceAll("--", "+");

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (getPriority(currentChar) == -5 ||
                    (i > 0 && currentChar == '-' && getPriority(line.charAt(i - 1)) != -5) || i == 0)
                digitTemp.append(currentChar);

            else {
                pushTemp();
                operators.insertElementAt(currentChar, 0);
            }
            operators.removeAll(List.of(new Character[]{'(', ')'}));
        } pushTemp();

        if (operators.size() == 0) return digits.size() == 0 ? Float.NaN : digits.get(0);
        else {
            prioritize();

            // to track the stacks, uncomment the following two lines.
             System.out.println(digits);
             System.out.println(operators);
            while (!operators.empty()) {
                try {
                    f1 = digits.pop();
                    f2 = digits.pop();
                } catch (EmptyStackException e) {
                    throw new SyntaxError();
                }

                result = operate(operators.lastElement(), f1, f2);
                digits.push(result);

                operators.pop();
            }
        } return result;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String input;

        System.out.println("Welcome to the statistical calculator MCNIEV-1.");
        System.out.println("Enter formulas, or \"HELP\" for a guide. \"OFF\" to quit.");

        while (true) {
            System.out.print("> ");
            input = in.nextLine().strip().toUpperCase(Locale.ROOT).replaceAll(" ", "");

            if (input.equals("OFF")) System.exit(0);
            if (input.equals("HELP")) {
                System.out.println("HELP MANUAL GUIDE");
                System.out.println("You can enter in expressions, like \"1 + 1\".");
                System.out.println("You can also enter in functions, including:");
                System.out.println("\tFACT() -> factorial of a number or expression.");
                System.out.println("\tSQRT() -> square root of a number or expression.");
            }
            else {
                try {
                    input = metaExec(input);
                    while (input.indexOf('(') != -1) input = metaExec(input);

                    history = calculate(input);

                    System.out.println(history);
                } catch (SyntaxError e) {
                    System.out.println("ERR: SYNTAX");
                } catch (ArgumentError e) {
                    System.out.println("ERR: ARGUMENT");
                } finally {
                    digits.clear();
                    operators.clear();
                }
            }
        }
    }
}
