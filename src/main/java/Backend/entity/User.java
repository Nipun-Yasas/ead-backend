import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor


public class User{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true , nullable = false)
    private String email;
    private String name;
    private String password;
    private boolean enabled=true;
}