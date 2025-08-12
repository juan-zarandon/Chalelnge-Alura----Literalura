package com.alura.Literalura.principal;

import com.alura.Literalura.models.*;
import com.alura.Literalura.repositorio.IAutoresRepository;
import com.alura.Literalura.repositorio.ILibrosRepository;
import com.alura.Literalura.service.ConsumoApi;
import com.alura.Literalura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final static String URL_BASE = "https://gutendex.com/books/?search=";

    private IAutoresRepository autoresRepository;
    private ILibrosRepository librosRepository;

    public Principal(IAutoresRepository autoresRepository, ILibrosRepository librosRepository) {
        this.autoresRepository = autoresRepository;
        this.librosRepository = librosRepository;
    }

    public void muestraElMenu () {
        var opcion = -1;
        System.out.println("Bienvenido! Por favor selecciona una opción: ");
        while (opcion != 0) {
            var menu = """
                    1 - | Buscar libros por título |
                    2 - | Listar libros registrados |
                    3 - | Listar autores registrados |
                    4 - | Listar autores vivos en un determinado año |
                    5 - | Listar libros por idioma |
                    0 - | Salir |
                    """;
            System.out.println(menu);
            try {
                opcion = teclado.nextInt();
                teclado.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Opción no válida. Por favor, ingresa un número.");
                teclado.nextLine(); // Limpiar el buffer del scanner
                continue;
            }

            switch (opcion) {
                case 1:
                    agregarLibros();
                    break;
                case 2:
                    librosRegistrados();
                    break;
                case 3:
                    autoresRegistrados();
                    break;
                case 4:
                    autoresPorAño();
                    break;
                case 5:
                    listarPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando aplicación...");
                    break;
                default:
                    System.out.println("Opción no válida, intenta de nuevo");
            }

        }
    }

    private Datos getDatosLibros() {
        System.out.println("Escribe el nombre del libro que deseas buscar: ");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "+"));
        return conversor.obtenerDatos(json, Datos.class);
    }

    private Libros crearLibro(DatosLibros datosLibros, Autores autor) {
        if (autor != null) {
            return new Libros(datosLibros, autor);
        } else {
            System.out.println("El autor es null, no se puede crear el libro");
            return null;
        }
    }

    private  void agregarLibros() {
        Datos datos = getDatosLibros();
        if (datos != null && !datos.resultados().isEmpty()) {
            DatosLibros datosLibro = datos.resultados().get(0);
            DatosAutores datosAutores = datosLibro.autor().get(0);
            Libros libroRepositorio = librosRepository.findByTitulo(datosLibro.titulo());
            if (libroRepositorio != null) {
                System.out.println("Este libro ya se encuentra en la base de datos");
                System.out.println(libroRepositorio);
            } else {
                Autores autorRepositorio = autoresRepository.findByNameIgnoreCase(datosLibro.autor().get(0).nombreAutor());
                if (autorRepositorio != null) {
                    Libros libro = crearLibro(datosLibro, autorRepositorio);
                    if (libro != null) {
                        librosRepository.save(libro);
                        System.out.println("----- LIBRO AGREGADO -----\n");
                        System.out.println(libro);
                    }
                } else {
                    Autores autor = new Autores(datosAutores);
                    autor = autoresRepository.save(autor);
                    Libros libro = crearLibro(datosLibro, autor);
                    if (libro != null) {
                        librosRepository.save(libro);
                        System.out.println("----- LIBRO AGREGADO -----\n");
                        System.out.println(libro);
                    }
                }
            }
        } else {
            System.out.println("El libro no existe en la API de Gutendex, ingresa otro");
        }
    }

    private void librosRegistrados() {
        List<Libros> libros = librosRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados");
            return;
        }
        System.out.println("----- LOS LIBROS REGISTRADOS SON: -----\n");
        libros.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }

    private void autoresRegistrados() {
        List<Autores> autores = autoresRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados");
            return;
        }
        System.out.println("----- LOS AUTORES REGISTRADOS SON: -----\n");
        autores.stream()
                .sorted(Comparator.comparing(Autores::getName))
                .forEach(System.out::println);
    }

    private void autoresPorAño() {
        System.out.println("Escribe el año en el que deseas buscar: ");
        int año = 0;
        try {
            año = teclado.nextInt();
            teclado.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Entrada no válida. Por favor, ingresa un número.");
            teclado.nextLine();
            return;
        }
        if(año < 0) {
            System.out.println("El año no puede ser negativo, intenta de nuevo");
            return;
        }
        List<Autores> autoresPorAño = autoresRepository.findByAñoNacimientoLessThanEqualAndAñoMuerteGreaterThanEqual(año, año);
        if (autoresPorAño.isEmpty()) {
            System.out.println("No hay autores registrados en ese año");
            return;
        }
        System.out.println("----- LOS AUTORES VIVOS REGISTRADOS EN EL AÑO " + año + " SON: -----\n");
        autoresPorAño.stream()
                .sorted(Comparator.comparing(Autores::getName))
                .forEach(System.out::println);
    }

    private void listarPorIdioma() {
        System.out.println("Escribe el idioma por el que deseas buscar: ");
        String menu = """
                es - Español
                en - Inglés
                fr - Francés
                pt - Portugués
                """;
        System.out.println(menu);
        var idioma = teclado.nextLine();
        if (!idioma.equals("es") && !idioma.equals("en") && !idioma.equals("fr") && !idioma.equals("pt")) {
            System.out.println("Idioma no válido, intenta de nuevo");
            return;
        }
        List<Libros> librosPorIdioma = librosRepository.findByLenguajesContaining(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No hay libros registrados en ese idioma");
            return;
        }
        System.out.println("----- LOS LIBROS REGISTRADOS EN EL IDIOMA SELECCIONADO SON: -----\n");
        librosPorIdioma.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }
}