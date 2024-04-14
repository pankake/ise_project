package utils;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

public class rand_int extends DefaultInternalAction {
    private static final Random RAND = new Random();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int min = (int) ((NumberTerm) args[1]).solve();
        int max = (int) ((NumberTerm) args[2]).solve();
        int result = RAND.nextInt(max - min) + min;
        return un.unifies(args[0], new NumberTermImpl(result));
    }
}
