package th.mfu.member;

/**
 * What a member looks like ON THE WIRE.
 * <p>
 * There is a second copy of this class in library-transaction-service. That is
 * deliberate, and it is worth a minute of your attention: the two services are
 * separate programs that only agree on a shape. Sharing one jar between them
 * would tie them together again, and then you would have two services that must
 * always be rebuilt and redeployed as a pair - which is the thing microservices
 * are trying to avoid.
 */
public class MemberDTO {

    private Long id;
    private String name;
    private String email;
    private int age;

    /**
     * Which copy of member-service answered. It is here only so that you can SEE
     * the load balancer working in step 6. Real code would not put this on the
     * wire.
     */
    private int servedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getServedBy() {
        return servedBy;
    }

    public void setServedBy(int servedBy) {
        this.servedBy = servedBy;
    }

}
