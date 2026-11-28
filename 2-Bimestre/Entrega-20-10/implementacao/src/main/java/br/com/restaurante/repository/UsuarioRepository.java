package br.com.restaurante.repository;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCaseAndIdNot(
            String login,
            Long id);

    List<Usuario> findAllByOrderByNomeAsc();

    List<Usuario> findByPerfilOrderByNomeAsc(
            Perfil perfil);

    List<Usuario> findByAtivoOrderByNomeAsc(
            boolean ativo);

    List<Usuario> findByBloqueadoOrderByNomeAsc(
            boolean bloqueado);

    long countByPerfilAndAtivoTrueAndBloqueadoFalse(
            Perfil perfil);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> buscarParaAtualizacao(
            @Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.login = :login")
    Optional<Usuario> buscarPorLoginParaAtualizacao(
            @Param("login") String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.perfil = :perfil ORDER BY u.id")
    List<Usuario> bloquearUsuariosPorPerfil(
            @Param("perfil") Perfil perfil);
}