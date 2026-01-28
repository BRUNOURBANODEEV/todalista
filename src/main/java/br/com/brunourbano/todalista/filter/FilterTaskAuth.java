package br.com.brunourbano.todalista.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.brunourbano.todalista.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        // 🔓 Libera OPTIONS (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔓 Se NÃO for /tasks, deixa passar
        if (!servletPath.startsWith("/tasks")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Basic ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization header ausente");
            return;
        }

        String authEncoded = authorization.substring(6);
        String authDecoded;

        try {
            authDecoded = new String(Base64.getDecoder().decode(authEncoded));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization inválido");
            return;
        }

        if (!authDecoded.contains(":")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Formato inválido de credenciais");
            return;
        }

        String[] credentials = authDecoded.split(":", 2);
        String username = credentials[0];
        String password = credentials[1];

        var user = userRepository.findByUsername(username);

        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Usuário não encontrado");
            return;
        }

        var passwordVerify = BCrypt.verifyer()
                .verify(password.toCharArray(), user.getPassword());

        if (!passwordVerify.verified) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Senha inválida");
            return;
        }

        // ✅ Usuário autenticado
        request.setAttribute("idUser", user.getId());
        filterChain.doFilter(request, response);
    }
}