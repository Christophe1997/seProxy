package chris.seProxy.rewriter;

import java.util.Optional;

public interface Rewriter {

    Optional<String> rewrite(String source);

}
