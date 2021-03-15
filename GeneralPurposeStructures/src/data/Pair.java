package data;

import java.util.Objects;

/**
 * Clasa care inglobeaza o pereche de doua elemente de tipuri template-izate.
 * @param <A> Tipul de date al primului membru al perechii.
 * @param <B> Tipul de date al celui de-al doilea membru al perechii.
 */
public class Pair<A, B> {
    /**
     * Primul membru al perechii.
     */
    private A first;

    /**
     * Al doilea membru al perechii.
     */
    private B second;


    /**
     * Constructorul clasei, care initializeaza cei doi membri.
     * @param first Primul membru
     * @param second Al doilea membru
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Getter pentru primul membru.
     * @return Primul membru.
     */
    public A getFirst() {
        return first;
    }
    /**
     * Setter pentru primul membru
     * @param first Valoarea primului membru.
     */
    public void setFirst(A first) {
        this.first = first;
    }

    /**
     * Getter pentru al doilea membru
     * @return Al doilea membru
     */
    public B getSecond() {
        return second;
    }
    /**
     * Setter pentur al doilea membru
     * @param second Valorea celui de-al doilea membru
     */
    public void setSecond(B second) {
        this.second = second;
    }

    /**
     * Functie care compara daca doua perechi sunt egale; se compara cei doi membro
     * @param o Obiectul cu care se face comparatia
     * @return True, daca cele doua obiecte sunt egale d.p.v. al membrilor, sau False in caz contrar
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    /**
     * Formatul de afisare al perechii
     * @return Formatul de afisare al perechii
     */
    @Override
    public String toString() {
        return String.format("%s : %d", this.first, this.second);
    }
}