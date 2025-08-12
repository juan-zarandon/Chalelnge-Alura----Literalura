package com.alura.Literalura.repositorio;

import com.alura.Literalura.models.Autores;
import com.alura.Literalura.models.Libros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IAutoresRepository extends JpaRepository<Autores, Long> {
    Autores findByNameIgnoreCase(String nombre);

    List<Autores> findByAñoNacimientoLessThanEqualAndAñoMuerteGreaterThanEqual(int añoInicial, int añoFinal);
}