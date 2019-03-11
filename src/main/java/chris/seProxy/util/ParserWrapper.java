package chris.seProxy.util;

import chris.seProxy.exception.ParserInitFailure;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.*;

/**
 * Parser Wrapper class
 *
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

    public ParserWrapper(Class<? extends Lexer> lexerClass, Class<? extends Parser> parserClass) {

        this.lexerClass = lexerClass;
        this.parserClass = parserClass;

    }

    public ParserWrapper(CharStream input,
                         Class<? extends Lexer> lexerClass, Class<? extends Parser> parserClass) throws Exception {

        this.lexerClass = lexerClass;
        this.parserClass = parserClass;
        init(input);

    }

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

    public void reinit() throws Exception {
        init(this.input);
    }

    public void setInput(CharStream input) throws Exception {
        this.input = input;
        reinit();
    }
}
