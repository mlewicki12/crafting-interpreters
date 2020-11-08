
package com.mlewicki12.lox;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;

public class OperatorMap {
    /*
     * this is an iffy solution that uses a double hashmap to assign a tuple of types to operators
     * I was messing around with a linked list of types and hashmaps for each operator, but that feels excessive
     * right now, especially since this works
     */
    private final HashMap<StringTuple, HashMap<String, Operator>> map = new HashMap<>();

    OperatorMap() {
        defineOperators();
    }

    public Object getOperator(String type, TokenType operator, Object value) {
        return getOperator(new StringTuple(type), operator, value);
    }

    public Object getOperator(String type1, String type2, TokenType operator, Object... values) {
        return getOperator(new StringTuple(type1, type2), operator, values);
    }

    private Object getOperator(StringTuple key, TokenType operator, Object... values) {
        if(map.containsKey(key)) {
            HashMap operatorMap = map.get(key);
            if(operatorMap.containsKey(operator.toString())) {
                Operator opr = (Operator)operatorMap.get(operator.toString());
                return opr.run(values);
            }
        }

        String[] types = new String[values.length];
        for(int i = 0; i < types.length; ++i) {
            types[i] = "Object";
        }

        StringTuple newKey = new StringTuple(types);
        if(map.containsKey(newKey) && map.get(newKey).containsKey(operator.toString())) {
            Operator opr = (Operator)map.get(newKey).get(operator.toString());
            return opr.run(values);
        } else {
            return null; // throw error
        }
    }

    public void defineOperators() {
        defineOperator("Double", TokenType.MINUS, new UnaryOperator((a) -> -(double)a));
        defineOperator("Object", TokenType.BANG, new UnaryOperator((a) -> !isTruthy(a)));

        defineOperator("Double", "Double", TokenType.MINUS, new BinaryOperator((a, b) -> (double)a - (double)b));
        defineOperator("Double", "Double", TokenType.SLASH, new BinaryOperator((a, b) -> (double)a / (double)b));
        defineOperator("Double", "Double", TokenType.STAR, new BinaryOperator((a, b) -> (double)a * (double)b));
        defineOperator("Double", "Double", TokenType.PLUS, new BinaryOperator((a, b) -> (double)a + (double)b));

        defineOperator("Double", "Double", TokenType.GREATER, new BinaryOperator((a, b) -> (double)a > (double)b));
        defineOperator("Double", "Double", TokenType.GREATER_EQUAL, new BinaryOperator((a, b) -> (double)a >= (double)b));
        defineOperator("Double", "Double", TokenType.LESS, new BinaryOperator((a, b) -> (double)a < (double)b));
        defineOperator("Double", "Double", TokenType.LESS_EQUAL, new BinaryOperator((a, b) -> (double)a <= (double)b));

        defineOperator("String", "String", TokenType.PLUS, new BinaryOperator((a, b) -> (String)a + (String)b));

        // this should work to pump the operators into one function, i just need a catch for it
        defineOperator("Object", "Object", TokenType.EQUAL_EQUAL, new BinaryOperator((a, b) -> isEqual(a, b)));
        defineOperator("Object", "Object", TokenType.BANG_EQUAL, new BinaryOperator((a, b) -> !isEqual(a, b)));
    }

    public void defineOperator(String type, TokenType operator, Operator function) {
        defineOperator(new StringTuple(type), operator, function);
    }

    public void defineOperator(String type1, String type2, TokenType operator, Operator function) {
        defineOperator(new StringTuple(type1, type2), operator, function);
    }

    public void defineOperator(StringTuple key, TokenType operator, Operator function) {
        if(!map.containsKey(key)) {
            map.put(key, new HashMap<>());
        }

        HashMap operatorMap = map.get(key);
        if(!map.containsKey(operator.toString())) {
            operatorMap.put(operator.toString(), function);
        } else {
            return; // throw error
        }
    }

    private boolean isTruthy(Object value) {
        if(value == null) return false;
        if(value instanceof Boolean) return (boolean)value;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null) return true;
        if(left == null) return false;

        return left.equals(right);
    }

    // apparently there isn't a native java tuple, so here is a quick one i made
    private class StringTuple {
        final String[] values;

        StringTuple(String... values) {
            this.values = values.clone();
        }

        public String toString() {
            return "(" + String.join(",", values) + ")";
        }

        // make it work with hashmaps
        @Override
        public int hashCode() {
            HashCodeBuilder builder = new HashCodeBuilder(2311, 3011); // two random numbers
            for(String val : values) {
                builder.append(val);
            }

            return builder.toHashCode();
        }

        @Override
        public boolean equals(Object other) {
            if(other == null || !(other instanceof StringTuple)) {
                return false;
            }

            StringTuple otherST = (StringTuple)other;

            // pad the shorter array
            String[] values1, values2;
            if(values.length > otherST.values.length) {
                values1 = values;
                values2 = otherST.values;
            } else {
                values1 = otherST.values;
                values2 = values;
            }

            String[] paddedValues2 = new String[values1.length];
            for(int i = 0; i < values2.length; ++i) {
                paddedValues2[i] = values2[i];
            }

            if(values2.length != values1.length) {
                for(int i = values2.length; i < values1.length; ++i) {
                    paddedValues2[i] = "";
                }
            }

            EqualsBuilder builder = new EqualsBuilder();
            for(int i = 0; i < values1.length; ++i) {
                builder.append(values1[i], paddedValues2[i]);
            }

            return builder.isEquals();
        }
    }

    interface Operator {
        public Object run(Object... values);
    }

    class UnaryOperator implements Operator {
        private final SingleOperatorFunction function;
        UnaryOperator(SingleOperatorFunction function) {
            this.function = function;
        }

        @Override
        public Object run(Object... values) {
            if(values.length != 1 ) {
                return false; // throw error
            }

            return this.function.run(values[0]);
        }
    }

    class BinaryOperator implements Operator {
        private final DoubleOperatorFunction function;
        BinaryOperator(DoubleOperatorFunction function) {
            this.function = function;
        }

        @Override
        public Object run(Object... values) {
            if(values.length != 2 ) {
                return false; // throw error
            }

            return this.function.run(values[0], values[1]);
        }
    }

    // not throwing ternary operator here yet, bc it has just one use in the language so far

    interface SingleOperatorFunction { // im sure there's a better way to do this, but this should let me define the operation as a lambda
        public Object run(Object value);
    }

    interface DoubleOperatorFunction {
        public Object run(Object left, Object right);
    }

}
