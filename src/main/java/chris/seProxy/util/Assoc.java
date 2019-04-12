package chris.seProxy.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Associated list, such as [(a1, b1), (a2, b2), ...]
 *
 * @param <A> first type a or Key
 * @param <B> second type b or Value
 */
public class Assoc<A, B> implements Iterable<Assoc<A, B>.AssocElem> {

    @NotNull
    @Override
    public Iterator<AssocElem> iterator() {
        return data.iterator();
    }

    class AssocElem {
        A fst;
        B snd;

        AssocElem(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }

    private List<AssocElem> data;

    public Assoc() {
        this.data = new ArrayList<>();
    }

    public void add(A fst, B snd) {
        data.add(new AssocElem(fst, snd));
    }

    public Optional<B> findSnd(A fst) {
        for (AssocElem e : data) {
            if (e.fst.equals(fst)) {
                return Optional.of(e.snd);
            }
        }
        return Optional.empty();
    }

    public Optional<A> findFst(B snd) {
        for (AssocElem e : data) {
            if (e.snd.equals(snd)) {
                return Optional.of(e.fst);
            }
        }
        return Optional.empty();
    }

    public void removeByFst(A fst) {
        int idx;
        for (idx = 0; idx < data.size(); idx++) {
            if (data.get(idx).fst.equals(fst)) {
                break;
            }
        }
        if (idx < data.size()) {
            data.remove(idx);
        }
    }

    public void removeBySnd(B snd) {
        int idx;
        for (idx = 0; idx < data.size(); idx++) {
            if (data.get(idx).snd.equals(snd)) {
                break;
            }
        }
        if (idx < data.size()) {
            data.remove(idx);
        }
    }

    public boolean existFst(A fst) {
        for (AssocElem e : data) {
            if (e.fst.equals(fst)) {
                return true;
            }
        }
        return false;
    }

    public boolean existSnd(B snd) {
        for (AssocElem e : data) {
            if (e.snd.equals(snd)) {
                return true;
            }
        }
        return false;
    }

    public Optional<A> getDefaultFst() {
        if (!data.isEmpty()) {
            return Optional.of(data.get(0).fst);
        } else {
            return Optional.empty();
        }
    }

    public Optional<B> getDefaultSnd() {
        if (!data.isEmpty()) {
            return Optional.of(data.get(0).snd);
        } else {
            return Optional.empty();
        }
    }

}
