package chris.seProxy.util;

import chris.seProxy.exception.ParserInitFailure;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.*;

/**
 * Parser Wrapper
 */
public class ParserWrapper {

    @Getter @Setter
    private Class<? extends Lexer> lexerClass;
    @Getter @Setter
    private Class<? extends Parser> parserClass;

    @Getter
    private CharStream input;
    @Getter
    private Lexer lexer;
    @Getter
    private Parser parser;
    @Getter
    private TokenStream tokens;

    /**
     * Specify used Lexer class and Parser class.
     * @param lexerClass Lexer Class
     * @param parserClass Parser Class
     */
    public ParserWrapper(Class<? extends Lexer> lexerClass, Class<? extends Parser> parserClass) {

        this.lexerClass = lexerClass;
        this.parserClass = parserClass;

    }

    /**
     * Specify used Lexer class and Parser class and init it with input
     * @param input input Stream
     * @param lexerClass Lexer Class
     * @param parserClass Parser Class
     * @throws Exception Throws exception from {@link chris.seProxy.util.ParserWrapper#init(CharStream)}
     */
    public ParserWrapper(CharStream input,
                         Class<? extends Lexer> lexerClass, Class<? extends Parser> parserClass) throws Exception {

        this.lexerClass = lexerClass;
        this.parserClass = parserClass;
        init(input);

    }

    /**
     * init with input
     * @param input input Stream
     * @throws Exception Throws a {@link ParserInitFailure} for some reasons, such as NoSuchMethodException.
     */
    public void init(CharStream input) throws Exception {

        this.input = input;
        try {
            lexer = lexerClass.getConstructor(CharStream.class).newInstance(input);
            tokens = new CommonTokenStream(lexer);
            parser = parserClass.getConstructor(TokenStream.class).newInstance(tokens);
        } catch (Exception ex) {
            throw new ParserInitFailure(ex.getMessage());
        }

    }

    /**
     * reinit with current input
     * @throws Exception Throws exception from {@link chris.seProxy.util.ParserWrapper#init(CharStream)}
     */
    public void reinit() throws Exception {
        init(this.input);
    }

    /**
     * Change Input and reinit
     * @param input input stream
     * @throws Exception Throws exception from {@link chris.seProxy.util.ParserWrapper#init(CharStream)}
     */
    public void setInput(CharStream input) throws Exception {
        this.input = input;
        reinit();
    }
}
