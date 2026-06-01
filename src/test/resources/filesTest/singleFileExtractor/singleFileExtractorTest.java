package truc.test;

import java.util.Map;
import java.util.List;

public class TestParsing <T extends Map<String, List<T>> & java.io.Serializable> {

    String $str1 = "/*"; // /*
    /* // */ char _c1 = '\''; 
    String $str2 = "\"";

    String TEXT_BLOCK = """
        Ici on a de fausses flèches -> et ->>
        De faux génériques <String> et des commentaires // /*
        Des " isolés et des "" ou même des \"\"\" échappés.
        """;

    public <U> boolean testMethod(int a, int b) {
        int _$$=42;int __=_$$+1;
        
        boolean trap1 = a<_$$&&__>b || java.util.Collections.<String>emptyList()!=null;
        
        boolean trap2 = (a<<2)>(b>>>1);
        
        return trap1 ^ trap2;
    }

    java.util.function.Function<Integer, Integer> lambda = (Integer i) -> {
        int x = 10;
        while (x -- > 0) { 
            i++; 
        }
        return switch(i) {
            case 1 -> i << 1;
            default -> { yield i >> 1; }
        };
    };
}