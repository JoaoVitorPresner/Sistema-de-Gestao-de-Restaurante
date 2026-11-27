package br.com.restaurante.security;

import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;
import br.com.restaurante.service.UsuarioAutenticado;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

public class ValidacaoSessaoFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarios;

    public ValidacaoSessaoFilter(
            UsuarioRepository usuarios) {

        this.usuarios = usuarios;
    }

    private String obterCaminho(
            HttpServletRequest request) {

        String uri = request.getRequestURI();

        String contexto = request.getContextPath();

        return uri.substring(contexto.length());
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String caminho = obterCaminho(request);

        return caminho.startsWith("/css/")
                || caminho.startsWith("/js/")
                || caminho.startsWith("/img/")
                || caminho.equals("/favicon.ico")
                || caminho.equals("/error")
                || caminho.equals("/login")
                || caminho.equals("/logout")
                || caminho.equals("/api/cliente/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            filterChain.doFilter(request, response);
            return;
        }

        if (!(authentication.getPrincipal()
                instanceof UsuarioAutenticado autenticado)) {

            filterChain.doFilter(request, response);
            return;
        }

        Usuario usuario = usuarios
                .findById(autenticado.getUsuarioId())
                .orElse(null);

        if (!sessaoValida(
                usuario,
                autenticado,
                authentication)) {

            invalidarSessao(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean sessaoValida(
            Usuario usuario,
            UsuarioAutenticado autenticado,
            Authentication authentication) {

        if (usuario == null
                || !usuario.ativo
                || usuario.bloqueado
                || usuario.perfil == null) {

            return false;
        }

        if (!Objects.equals(
                usuario.login,
                autenticado.getUsername())) {

            return false;
        }

        if (usuario.versaoSeguranca
                != autenticado.getVersaoSeguranca()) {

            return false;
        }

        String perfilEsperado =
                "ROLE_" + usuario.perfil.name();

        return authentication.getAuthorities().size() == 1
                && authentication.getAuthorities()
                    .stream()
                    .anyMatch(autoridade ->
                        perfilEsperado.equals(
                            autoridade.getAuthority()
                        )
                    );
    }

    private void invalidarSessao(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();

        HttpSession sessao =
                request.getSession(false);

        if (sessao != null) {

            sessao.invalidate();
        }

        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        if (obterCaminho(request).startsWith("/api/")) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            response.getWriter().write(
                    "{\"erro\":\"Sessao invalidada. Realize o login novamente.\","
                    + "\"status\":401}"
            );

            return;
        }

        response.sendRedirect(
                request.getContextPath()
                + "/index.html?sessaoInvalidada"
        );
    }
}