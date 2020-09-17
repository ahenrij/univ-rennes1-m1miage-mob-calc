package com.ad.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    public enum Operator { ADD, SUB, MUL, DIV, MOD }

    public static final String CURR_OP_KEY = "currentOperatorKey";
    public static final String CURR_OP1_KEY = "currentOp1Key";
    public static final String CURR_OP2_KEY = "currentOp2Key";

    private TextView tvDisplay;
    private ImageView imgBackspace;

    private String operand1, operand2;
    private Operator operator;
    private boolean isOn1; //is typing operand 1
    private boolean shouldStartOver = false; //If there was a calculation from equal btn, and a number is typed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeStatusBarColor();

        tvDisplay = findViewById(R.id.tv_display);
        imgBackspace = findViewById(R.id.img_backspace);

        initValues(savedInstanceState);
    }

    private void initValues(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            operator = (Operator) savedInstanceState.getSerializable(CURR_OP_KEY);
            operand1 = savedInstanceState.getString(CURR_OP1_KEY, "");
            operand2 = savedInstanceState.getString(CURR_OP2_KEY, "");
            isOn1 = (operator == null);
        } else {
            resetValues();
        }

        imgBackspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clearAll(view);
                return true;
            }
        });
        updateDisplay();
    }

    /**
     * setOperator holded by view to be the current operator
     * @pre view hold an operator in text, and !operand1.isEmpty()
     * @param view
     */
    public void addOperator(View view) {

        if (!operand1.isEmpty()) {

            if (operator != null && !operand2.isEmpty()) {
                compute();
            }
            int operatorId = view.getId();
            setOperator(operatorId);
            updateDisplay();
        }
    }

    private void setOperator(int operatorId) {
        isOn1 = false;
        switch (operatorId) {
            case R.id.op_add:
                operator = Operator.ADD;
                break;
            case R.id.op_sub:
                operator = Operator.SUB;
                break;
            case R.id.op_mul:
                operator = Operator.MUL;
                break;
            case R.id.op_div:
                operator = Operator.DIV;
                break;
            case R.id.op_mod:
                operator = Operator.MOD;
                break;
            default:
                Toast.makeText(this, "Opérateur non reconnu", Toast.LENGTH_SHORT).show();
                isOn1 = true;
        }
    }

    /**
     * @param view Button holding the single number to append as text
     * @post Append number or . to display if allowed (and appropriate operand)
     */
    public void addNumber(View view) {
        String number = ((Button) view).getText().toString();
        String operand = (isOn1) ? operand1 : operand2;

        if (number.equals(".")) {
            if (!operand.contains(".")) {
                if (operand.isEmpty()) {
                    operand = "0";
                }
                operand += ".";
            }
        } else {
            if (shouldStartOver) { //happen only after compute from equal btn
                operand = number;
                shouldStartOver = false;
            } else {
                operand = appendTo(operand, number);
            }
        }

        if (isOn1) {
            operand1 = operand;
        } else {
            operand2 = operand;
        }
        updateDisplay();
    }

    /**
     * Remove last char entered
     * @param view
     */
    public void doBackSpace(View view) {

        if (!isOn1) { //isOn operand 2 or operator

            if (operand2.isEmpty()) { //is on operator edit
                operator = null;
                isOn1 = true;
            } else { //remove last number
                operand2 = operand2.substring(0, operand2.length() - 1);
            }
        } else { //on operand 1
            if (!operand1.isEmpty()) { //if is not empty, remove last number
                operand1 = operand1.substring(0, operand1.length() - 1);
            }
        }

        updateDisplay();
    }

    /**
     * Reset everything and clear the display
     * @param view
     */
    public void clearAll(View view) {
        resetValues();
        updateDisplay();
    }

    /**
     * on btnEqual clicked
     * @param view
     */
    public void doCalculation(View view) {
        shouldStartOver = compute();
    }

    /**
     * On Unary minus clicked
     * @param view
     */
    public void toggleUnaryMinus(View view) {
        if (isOn1) {
            if (!operand1.isEmpty()) {
                operand1 = toggleOperandSign(operand1);
            }
        } else {
            if (operator!= null) {
                if (operator == Operator.SUB) {
                    operator = Operator.ADD;
                } else if (operator == Operator.ADD) {
                    operator = Operator.SUB;
                } else {
                    operand2 = toggleOperandSign(operand2);
                }
            }
        }
        updateDisplay();
    }

    private String toggleOperandSign(String operand) {
        return (operand.startsWith(getString(R.string.op_sub))) ? operand.substring(1) : getString(R.string.op_sub)+operand;
    }

    private void resetValues() {
        operand1 = "";
        operand2 = "";
        operator = null;
        isOn1 = true;
    }


    /**
     * @return true if display has been properly evaluated, and false + toast error message if not
     * @pre operand 1, operator and operand2 are defined
     * @post calculate expression displayed and set it to display : operand1 <= result, operator = null, && operand2 = ""
     */
    private boolean compute() {

        if (!isOn1 && !operand2.isEmpty()) {

            double result = 0.0, op1 = 0.0, op2 = 0.0;
            try {
                op1 = Double.parseDouble(operand1);
                op2 = Double.parseDouble(operand2);
            } catch (NumberFormatException | ClassCastException e) {
                Toast.makeText(this, "Valeur d'opérande erronée", Toast.LENGTH_LONG).show();
                return false;
            }

            switch (this.operator) {
                case ADD:
                    result = op1 + op2;
                    break;
                case SUB:
                    result = op1 - op2;
                    break;
                case DIV:
                    if (Double.parseDouble(operand2) == 0.0) {
                        Toast.makeText(this, "Division par zéro invalide", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    result = op1 / op2;
                    break;
                case MUL:
                    result = op1 * op2;
                    break;
                case MOD:
                    result = mod((int) op1, (int) op2);
                    break;
                default:
                    Toast.makeText(this, "Operateur non reconnu.", Toast.LENGTH_SHORT).show();
                    return false;
            }

            String re = String.valueOf(result);

            //if result if integer, show it as int
            if (doubleIsInteger(result)) {
                if (((long)result) == result) {
                    operand1 = String.valueOf((long) result);
                } else {
                    operand1 = String.valueOf(result);
                }
            } else {
                //Show double result with precision of result_precision integer after . if needed
                operand1 = (new DecimalFormat("0.###")).format(result).replace(",", ".");
            }

            operand2 = "";
            operator = null;
            isOn1 = true;

            updateDisplay();
            return true;
        }
        return false;
    }

    /**
     * Update the display according to current values of operands and operator
     */
    private void updateDisplay() {

        String toDisplay = "";
        if (!operand1.isEmpty()) { //Append operand 1 if exists
            toDisplay += operand1;

            if (!isOn1) { //There is an operator added, append it
                if (operator != null) {
                    toDisplay +=  "<font color=\"#8BC34A\">";
                    switch (operator) {
                        case ADD:
                            toDisplay += getResources().getString(R.string.op_add);
                            break;
                        case SUB:
                            toDisplay += getResources().getString(R.string.op_sub);
                            break;
                        case DIV:
                            toDisplay += getResources().getString(R.string.op_div);
                            break;
                        case MUL:
                            toDisplay += getResources().getString(R.string.op_mul);
                            break;
                        case MOD:
                            toDisplay += getResources().getString(R.string.op_mod);
                            break;
                        default:
                    }
                    toDisplay +=  "</font>";

                    if (!operand2.isEmpty()) { // Append operand 2 if exists
                        toDisplay += operand2;
                    }
                }
            }
        }
        tvDisplay.setText(Html.fromHtml(toDisplay));
    }

    private String appendTo(String str, String toAppend) {
        return (str.equals("0") ? toAppend : str.concat(toAppend));
    }

    /**
     * @return x modulo y
     */
    private int mod(int x, int y) {
        int result = x % y;
        return result < 0 ? result + y : result;
    }

    /**
     * @param result (of operation)
     * @return true if result is an integer (.0)
     */
    private boolean doubleIsInteger(Double result) {
        return (result == Math.floor(result)) && !Double.isInfinite(result);
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#bdbdbd"));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURR_OP_KEY, operator);
        outState.putString(CURR_OP1_KEY, operand1);
        outState.putString(CURR_OP2_KEY, operand2);
    }
}