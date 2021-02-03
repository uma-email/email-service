package org.acme.emailservice.model;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.acme.emailservice.model.enums.ELabelRole;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "label")
@NamedQuery(name = "Label.getAll", query = "SELECT l FROM Label l ORDER BY l.name")
@NamedQuery(name = "Label.getAllUserLabels", query = "SELECT l FROM Label l WHERE l.user=:user ORDER BY l.name")
@NamedQuery(name = "Label.getUserDraftsLabel", query = "SELECT l FROM Label l WHERE l.user=:user AND l.role='DRAFTS'")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = true)
    private Label parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Label> children = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonbTransient
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "labels")
    @JsonbTransient
    private Set<Message> messages = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "labels")
    @JsonbTransient
    private Set<Filter> filters = new LinkedHashSet<>();

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ELabelRole role;

    @Column(nullable = true)
    private Integer color;

    @Column(nullable = false)
    @SequenceGenerator(name = "labelHistoryId", sequenceName = "label_history_id")
    @GeneratedValue(generator = "labelHistoryId", strategy = GenerationType.SEQUENCE)
    private Long historyId;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Byte lastStmt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()")
    private Date timestamp;

    public void addChild(Label child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Label child) {
        children.remove(child);
        child.setParent(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Label> getChild() {
        return children;
    }

    public void setChild(Set<Label> children) {
        this.children = children;
    }

    public Label getParent() {
        return parent;
    }

    public void setParent(Label parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ELabelRole getRole() {
        return role;
    }

    public void setRole(ELabelRole role) {
        this.role = role;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public Set<Filter> getFilters() {
        return filters;
    }

    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Byte getLastStmt() {
        return lastStmt;
    }

    public void setLastStmt(Byte lastStmt) {
        this.lastStmt = lastStmt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Label))
            return false;

        return id != null && id.equals(((Label) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
