package com.ad.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private int currentOperatorId;
    private String currentOperator;
    private String currentDisplay;
    private EditText etDisplay;

    private String OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_MOD;

    private String[] operators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDisplay = findViewById(R.id.et_display);

        initValues();

    }

    private void initValues() {

        OP_ADD = getResources().getString(R.string.op_add);
        OP_SUB = getResources().getString(R.string.op_sub);
        OP_MUL = getResources().getString(R.string.op_mul);
        OP_DIV = getResources().getString(R.string.op_div);
        OP_MOD = getResources().getString(R.string.op_mod);

        operators = new String[]{OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_MOD};

        setCurrentDisplay("");
        setCurrentOperator("");
    }

    public void addOperator(View view) {

        if (!currentDisplay.isEmpty()) {

            String operator = ((Button) view).getText().toString();
            Integer operatorId = view.getId();

            //No operator in the display
            if (displayContainsOperator()) {

                //Operator at the display's end
                if (displayEndsWithOperator()) {
                    setCurrentDisplay(currentDisplay.substring(0, currentDisplay.length()-1) + operator);
                } else {

                    //There is an operator not at the end
                    evaluateDisplay();
                    addOperatorToDisplay(operator, operatorId);
                }

            } else {
                addOperatorToDisplay(operator, operatorId);
            }
        }
    }

    public void addNumber(View view) {
        String number = ((Button) view).getText().toString();
        setCurrentDisplay(currentDisplay + number);
    }

    public void doBackSpace(View view) {
        if (!currentDisplay.isEmpty()) {
            setCurrentDisplay(currentDisplay.substring(0, currentDisplay.length() - 1));
        }
    }

    public void clearAll(View view) {
        setCurrentDisplay("");
    }

    public void doCalculation(View view) {

        if (displayEndsWithOperator()) {
            Toast.makeText(this, "Un format non valide est utilisé.", Toast.LENGTH_SHORT).show();
        } else if (displayContainsOperator()) {
            evaluateDisplay();
        }
    }

    private void setCurrentDisplay(String currentDisplay) {
        this.currentDisplay = currentDisplay;
        etDisplay.setText(currentDisplay);
    }

    private void setCurrentOperator(String currentOperator) {
        this.currentOperator = currentOperator;
    }

    private boolean displayContainsOperator() {

        boolean contains = false;
        for (String operator: operators) {
            if (currentDisplay.contains(operator)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private boolean displayEndsWithOperator() {

        boolean endsWith = false;
        for (String operator: operators) {
            if (currentDisplay.endsWith(operator)) {
                endsWith = true;
                break;
            }
        }
        return endsWith;
    }


    private void addOperatorToDisplay(String operator, Integer id) {
        if (!displayContainsOperator()) {
            setCurrentDisplay(currentDisplay + operator);
            setCurrentOperator(operator);
            this.currentOperatorId = id;
        }
    }

    /**
     * @pre display contains an operator not at the end : an operation !!
     * @post calculate expression display and set it to display
     */
    private void evaluateDisplay() {

        //Quote operator to avoid special characters regex exception
        String[] operands = currentDisplay.split(Pattern.quote(this.currentOperator));
        double result = 0;

        switch (this.currentOperatorId) {
            case R.id.op_add:
                result = Double.parseDouble(operands[0]) + Double.parseDouble(operands[1]);
                break;
            case R.id.op_sub:
                result = Double.parseDouble(operands[0]) - Double.parseDouble(operands[1]);
                break;
            case R.id.op_div:
                result = Double.parseDouble(operands[0]) / Double.parseDouble(operands[1]);
                break;
            case R.id.op_mul:
                result = Double.parseDouble(operands[0]) * Double.parseDouble(operands[1]);
                break;
            case R.id.op_mod:
                result = Double.parseDouble(operands[0]) % Double.parseDouble(operands[1]);
                break;
        }

        setCurrentDisplay(String.valueOf(result));
    }
}