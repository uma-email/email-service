package org.acme.emailservice.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.ColumnDefault;
// import org.jboss.logging.Logger;
// import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "message")
// @DynamicUpdate
@NamedQuery(name = "Message.get", query = "SELECT m FROM Message m WHERE m.account.user.username=:username AND m.id=:id")
@NamedQuery(name = "Message.getAll", query = "SELECT m FROM Message m WHERE m.account.user.username=:username ORDER BY m.timelineId DESC")
@NamedQuery(name = "Message.getAllByAccount", query = "SELECT m FROM Message m WHERE m.account.user.username=:username AND m.account=:account ORDER BY m.timelineId DESC")
public class Message {

    // private static Logger log = Logger.getLogger(Message.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @Column(nullable = true)
    private String messageId;

    @Column(nullable = true)
    private String threadId;

    @JsonbTransient
    @Column(columnDefinition = "jsonb", nullable = true)
    private String InReplyTo;

    @JsonbTransient
    @Column(name = "\"references\"", columnDefinition = "jsonb", nullable = true)
    private String references;

    @JsonbTransient
    @Column(nullable = true)
    private Boolean fwd;

    @Column(nullable = true)
    private String subject;

    @Column(nullable = true)
    private String snippet;

    @Column(nullable = true)
    private String resourceUrl;

    @Column(nullable = true)
    private String mimetype;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipientTo> recipientsTo = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipientCc> recipientsCc = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipientBcc> recipientsBcc = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "message_label", joinColumns = @JoinColumn(name = "message_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "label_id", referencedColumnName = "id"))
    private Set<Label> labels = new LinkedHashSet<>();

    @Column(nullable = true)
    private LocalDateTime sentAt;

    @Column(nullable = true)
    private LocalDateTime receivedAt;

    @Column(nullable = true)
    private LocalDateTime snoozedAt;

    @SequenceGenerator(name = "messageTimelineId", sequenceName = "message_timeline_id")
    @GeneratedValue(generator = "messageTimelineId", strategy = GenerationType.SEQUENCE)
    private Long timelineId;

    @SequenceGenerator(name = "messageHistoryId", sequenceName = "message_history_id")
    @GeneratedValue(generator = "messageHistoryId", strategy = GenerationType.SEQUENCE)
    private Long historyId;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Byte lastStmt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()")
    private Date timestamp;

    public Message() {

    }

    public void addRecipientTo(RecipientTo recipientTo) {
        recipientsTo.add(recipientTo);
        recipientTo.setMessage(this);
    }

    public void removeRecipientTo(RecipientTo recipientTo) {
        recipientsTo.remove(recipientTo);
        recipientTo.setMessage(null);
    }

    public void addRecipientCc(RecipientCc recipientCc) {
        recipientsCc.add(recipientCc);
        recipientCc.setMessage(this);
    }

    public void removeRecipientCc(RecipientCc recipientCc) {
        recipientsCc.remove(recipientCc);
        recipientCc.setMessage(null);
    }

    public void addRecipientBcc(RecipientBcc recipientBcc) {
        recipientsBcc.add(recipientBcc);
        recipientBcc.setMessage(this);
    }

    public void removeRecipientBcc(RecipientBcc recipientBcc) {
        recipientsBcc.remove(recipientBcc);
        recipientBcc.setMessage(null);
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }

    public void removeAttachment(Attachment attachment) {
        attachments.remove(attachment);
        attachment.setMessage(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.setMessage(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.setMessage(null);
    }

    public void addLabel(Label label) {
        labels.add(label);
        label.getMessages().add(this);
    }

    public void removeLabel(Label label) {
        labels.remove(label);
        label.getMessages().remove(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getInReplyTo() {
        return InReplyTo;
    }

    public void setInReplyTo(String InReplyTo) {
        this.InReplyTo = InReplyTo;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public Boolean getFwd() {
        return fwd;
    }

    public void setFwd(Boolean fwd) {
        this.fwd = fwd;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public List<RecipientTo> getRecipientsTo() {
        return recipientsTo;
    }

    public void setRecipientsTo(List<RecipientTo> recipientsTo) {
        this.recipientsTo = recipientsTo;
    }

    public List<RecipientCc> getRecipientsCc() {
        return recipientsCc;
    }

    public void setRecipientsCc(List<RecipientCc> recipientsCc) {
        this.recipientsCc = recipientsCc;
    }

    public List<RecipientBcc> getRecipientsBcc() {
        return recipientsBcc;
    }

    public void setRecipientsBcc(List<RecipientBcc> recipientsBcc) {
        this.recipientsBcc = recipientsBcc;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getSnoozedAt() {
        return snoozedAt;
    }

    public void setSnoozedAt(LocalDateTime snoozedAt) {
        this.snoozedAt = snoozedAt;
    }

    public Long getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(Long timelineId) {
        this.timelineId = timelineId;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

        if (!(o instanceof Message))
            return false;

        return id != null && id.equals(((Message) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
