package jburg.burg.ir;

/**
 * FormalParameter describes a (type, name) formal parameter pair.
 */
public class FormalParameter
{
    private FormalParameter(Object type, Object name)
    {
        this.type = type;
        this.name = name;
    }

    public final Object type;
    public final Object name;

    /**
     * Build a formal parameter array.
     * @param rawParameters a variadic list of type/name pairs.
     * @return the raw parameters, organized into an array of formals.
     */
    public static FormalParameter[] buildFormals(Object... rawParameters)
    {
        if (rawParameters.length % 2 != 0) {
            throw new IllegalArgumentException("Internal error: formal parameters must be specified in type/name pairs.");
        }

        FormalParameter[] result = new FormalParameter[rawParameters.length/2];

        for (int i = 0; i < result.length; i++) {
            int rawIdx = i*2;
            result[i] = new FormalParameter(rawParameters[rawIdx], rawParameters[rawIdx+1]);
        }

        return result;
    }

    public String toString()
    {
        return String.format("[%s,%s]", this.type, this.name);
    }
}
