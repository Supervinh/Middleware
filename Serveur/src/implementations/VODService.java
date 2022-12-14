/*
 @author: Bourdeau Quentin
          Faucher Vinh

 */
package implementations;

import contrats.FilmNotFoundException;
import contrats.IClientBox;
import contrats.IVODService;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CLasse implementant l'interface IVODService et contenant le catalogue de films
 */
public class VODService extends UnicastRemoteObject implements IVODService {

    private final List<MovieDesc> Catalog;
    private final List<Movie> listMovies;

    VODService(int port) throws RemoteException {
        super(port);
        Catalog = new ArrayList<>();
        listMovies = new ArrayList<>();
        inizialize();
    }

    /*
     * Methode permettant d'envoyer le catalogue de films au client
     */
    @Override
    public List<MovieDesc> viewCatalog() throws RemoteException {
        return Catalog;
    }

    /*
     * Methode permettant de lancer le film voulu à partir de son ISBN
     */
    @Override
    public Bill playMovie(String isbn, IClientBox box) throws RemoteException, FilmNotFoundException {
        Movie chosen = null;
        MovieDesc chosenDesc = null;
        for (Movie m : listMovies) {
            if (m.getIsbn().equals(isbn)) {
                chosen = m;
                break;
            }
        }
        for (MovieDesc md : Catalog) {
            if (md.getIsbn().equals(isbn)) {
                chosenDesc = md;
                break;
            }
        }
        if (chosen == null || chosenDesc == null) {
            throw new FilmNotFoundException();
        }

        byte[] data = chosen.getContent();
        int nbByte = 10;


        // on teste que la communication marche avant d'envoyer le Bill
        box.stream(Arrays.copyOfRange(data, 0, nbByte));

        new Thread(() -> {
            // pour gerer le paiement on devrait attendre une confirmation que le client a paye ici
            for (int index = nbByte; index < data.length; index += nbByte) {
                try {
                    box.stream(Arrays.copyOfRange(data, index, Math.min(index + nbByte, data.length)));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return new Bill(12.0F, chosenDesc.getMovieName());
    }

    /*
     * Methode permettant d'initialiser le catalogue de films
     */
    void inizialize() {
        Catalog.add(new MovieDesc("The Godfather", "108-2-506-907-98-6", "En 1945, à New York, les Corleone sont une des 5 familles de la mafia. Don Vito Corleone, `parrain' de cette famille, marie sa fille à un bookmaker. Sollozzo, `parrain' de la famille Tattaglia, propose à Don Vito une association dans le trafic de drogue, mais celui-ci refuse. Sonny, un de ses fils, y est quant à lui favorable. Afin de traiter avec Sonny, Sollozzo tente de faire tuer Don Vito, mais celui-ci en réchappe.", new Bill(5, "The Godfather")));
        Catalog.add(new MovieDesc("Kenshin : L'Achèvement", "250-5-754-680-95-6", "En 1879, Kenshin se bat avec ses alliés contre son plus grand ennemi : son ancien beau-frère Enishi Yukishiro, entouré de ses sbires qui ont juré de se venger.", new Bill(7, "Kenshin : L'Achèvement")));
        Catalog.add(new MovieDesc("The Dark Knight", "978-2-364-800-65-6", "Batman, le chevalier noir, est de retour. Il est aidé par le lieutenant Jim Gordon et le procureur Harvey Dent, qui ont décidé de nettoyer la ville de ses criminels. Mais le Joker, un dangereux psychopathe, est déterminé à faire régner la terreur et à détruire la ville.", new Bill(10, "The Dark Knight")));
        Catalog.add(new MovieDesc("The Lord of the Rings: The Return of the King", "500-7-354-674-40-7", "Frodon et Sam continuent leur route vers la Comté, accompagnés de Gollum. Pendant ce temps, Aragorn, Legolas et Gimli, rejoints par Merry et Pippin, partent à la rescousse d'Éowyn et Théoden, prisonniers de Saroumane, tandis que Gandalf affronte le Nazgûl.", new Bill(8, "The Lord of the Rings: The Return of the King")));
        Catalog.add(new MovieDesc("The Lord of the Rings: The Fellowship of the Ring", "700-3-456-987-12-3", "Frodon, un hobbit, hérite d'un anneau magique. Il est alors entraîné dans une aventure pour détruire l'anneau avant qu'il ne tombe entre les mains du Seigneur des ténèbres.", new Bill(8, "The Lord of the Rings: The Fellowship of the Ring")));

        listMovies.add(new Movie("108-2-506-907-98-6", "En 1945, à New York, les Corleone sont une des 5 familles de la mafia.".getBytes(StandardCharsets.UTF_8)));
        listMovies.add(new Movie("250-5-754-680-95-6", "En 1879, Kenshin se bat avec ses alliés contre son plus grand ennemi.".getBytes(StandardCharsets.UTF_8)));
        listMovies.add(new Movie("978-2-364-800-65-6", "Batman, le chevalier noir, est de retour. Il est aidé par le lieutenant Jim Gordon.".getBytes(StandardCharsets.UTF_8)));
        listMovies.add(new Movie("500-7-354-674-40-7", "Frodon et Sam continuent leur route vers la Comté, accompagnés de Gollum.".getBytes(StandardCharsets.UTF_8)));
        listMovies.add(new Movie("700-3-456-987-12-3", "Frodon, un hobbit, hérite d'un anneau magique. Il est alors entraîné dans une aventure pour détruire l'anneau".getBytes(StandardCharsets.UTF_8)));
    }
}
