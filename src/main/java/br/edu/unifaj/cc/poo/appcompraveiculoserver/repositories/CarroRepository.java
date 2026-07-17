package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CarroRepository {

    private final JdbcTemplate jdbc;

    public CarroRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Carro> ROW_MAPPER = (rs, rowNum) -> {
        Carro c = new Carro();
        c.setId(rs.getLong("id"));
        c.setCarroNome(rs.getString("carroNome"));
        c.setCarroCor(rs.getString("carroCor"));
        c.setCarroAno(rs.getInt("carroAno"));
        c.setCarroValor(rs.getFloat("carroValor"));
        c.setCarroImagem(rs.getString("carroImagem"));
        c.setCarroAnunciadoEm(rs.getTimestamp("carroAnunciadoEm").toLocalDateTime());
        c.setCarroAtualizadoEm(rs.getTimestamp("carroAtualizadoEm").toLocalDateTime());
        c.setLoginId(rs.getLong("login_id"));
        return c;
    };

    public List<Carro> findAll() {
        return jdbc.query("SELECT * FROM carro ORDER BY id", ROW_MAPPER);
    }

    public Optional<Carro> findById(Long id) {
        return jdbc.query("SELECT * FROM carro WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<Carro> findTop3ByOrderByIdDesc() {
        return jdbc.query("SELECT * FROM carro ORDER BY id DESC LIMIT 3", ROW_MAPPER);
    }

    public List<Carro> findByLoginId(Long loginId) {
        return jdbc.query("SELECT * FROM carro WHERE login_id = ?", ROW_MAPPER, loginId);
    }

    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM carro WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public Carro save(Carro carro) {
        return carro.getId() == null ? insert(carro) : update(carro);
    }

    private Carro insert(Carro carro) {
        LocalDateTime agora = LocalDateTime.now();
        carro.setCarroAnunciadoEm(agora);
        carro.setCarroAtualizadoEm(agora);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO carro (carroNome, carroCor, carroAno, carroValor, carroImagem, carroAnunciadoEm, carroAtualizadoEm, login_id) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, carro.getCarroNome());
            ps.setString(2, carro.getCarroCor());
            ps.setInt(3, carro.getCarroAno());
            ps.setFloat(4, carro.getCarroValor());
            ps.setString(5, carro.getCarroImagem());
            ps.setTimestamp(6, Timestamp.valueOf(carro.getCarroAnunciadoEm()));
            ps.setTimestamp(7, Timestamp.valueOf(carro.getCarroAtualizadoEm()));
            ps.setLong(8, carro.getLoginId());
            return ps;
        }, keyHolder);

        carro.setId(keyHolder.getKey().longValue());
        return carro;
    }

    private Carro update(Carro carro) {
        carro.setCarroAtualizadoEm(LocalDateTime.now());
        jdbc.update("UPDATE carro SET carroNome=?, carroCor=?, carroAno=?, carroValor=?, carroImagem=?, carroAtualizadoEm=? WHERE id=?",
                carro.getCarroNome(), carro.getCarroCor(), carro.getCarroAno(), carro.getCarroValor(),
                carro.getCarroImagem(), Timestamp.valueOf(carro.getCarroAtualizadoEm()), carro.getId());
        return carro;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM carro WHERE id = ?", id);
    }
}