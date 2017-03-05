package pond.common.f;

import pond.common.S;

/**
 * Created by ed on 3/5/17.
 */
public class Generator {

    public static void genCallback() {
        S._for(S.range(0, 128)).each(i -> {
            System.out.printf("interface C%d<", i);
            S._for(S.range(1, i)).each(
                    ii -> {
                        System.out.printf("P%d", ii);
                        if (ii != i)
                            System.out.printf(", ");
                    }
            );
            System.out.printf("> { void apply(");
            S._for(S.range(1, i)).each(
                    iii -> {
                        System.out.printf("P%d p%d", iii, iii);
                        if (iii != i)
                            System.out.printf(", ");
                    }
            );
            System.out.printf("); }\n");
        });
    }

    public static void genFunc() {
        S._for(S.range(0, 128)).each(i -> {
            System.out.printf("interface F%d<R, ", i);
            S._for(S.range(1, i)).each(
                    ii -> {
                        System.out.printf("P%d", ii);
                        if (ii < i)
                            System.out.printf(", ");
                    }
            );
            System.out.printf("> { R apply(");
            S._for(S.range(1, i)).each(
                    iii -> {
                        System.out.printf("P%d p%d", iii, iii);
                        if (iii < i)
                            System.out.printf(", ");
                    }
            );
            System.out.printf("); }\n");
        });
    }
    /*
       public static <A, B, C, D, E>
    ParametrisedCtxHandler def(ParamDef<A> def_A,
                               ParamDef<B> def_B,
                               ParamDef<C> def_C,
                               ParamDef<D> def_D,
                               ParamDef<E> def_E,
                               Callback.C6<Ctx, A, B, C, D, E> handler) {
        return new ParametrisedCtxHandler(S.array(def_A, def_B),
                ctx -> handler.apply(ctx, def_A.get(ctx), def_B.get(ctx), def_C.get(ctx), def_D.get(ctx), def_E.get(ctx)));
    }
     */

    public static void genDef() {
        S._for(S.range(0, 128)).each(i -> {
            System.out.printf("public static <", i);
            S._for(S.range(1, i)).each(
                    ii -> {
                        System.out.printf("P%d", ii);
                        if (ii < i)
                            System.out.printf(", ");
                    }
            );
            System.out.printf("> ParametrisedCtxHandler def(");
            S._for(S.range(1, i)).each(
                    iii -> {
                        System.out.printf("ParamDef<P%d> def_%d", iii, iii);
                        System.out.printf(", ");
                    }
            );
            System.out.printf("Callback.C%d<Ctx,", i + 1);
            S._for(S.range(1, i)).each(
                    iiii -> {
                        System.out.printf("P%d", iiii, iiii);
                        if (iiii < i)
                            System.out.printf(", ");
                    }
            );

            System.out.printf("> handler) { ");
            System.out.printf(" return new ParametrisedCtxHandler(S.array(%s), ctx-> handler.apply(ctx, %s));",
                    String.join(",", S._for(S.range(1, i)).map(_i -> String.format("def_%d", _i))),
                    String.join(",", S._for(S.range(1, i)).map(_i -> String.format("def_%d.get(ctx)", _i)))
            );
            System.out.printf("}\n");
        });
    }

    public static void main(String[] arg) {
        genDef();

    }
}
