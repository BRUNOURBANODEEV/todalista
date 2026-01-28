package br.com.brunourbano.todalista.user;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_users")
public class UserModel {

    @Id
    @GeneratedValue
    private UUID id;

    private String username;
    private String password;

    // 🔽 GETTERS E SETTERS
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
