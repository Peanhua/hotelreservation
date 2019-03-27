/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

/**
 *
 * @author joyr
 */
public class Tuple<A, B> {
    public A first;
    public B second;

    public Tuple(A first, B second) {
        this.first  = first;
        this.second = second;
    }
}
