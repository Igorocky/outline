package org.igye.outline.data;

import com.google.common.collect.ImmutableSet;
import fj.F2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditSynopsisTopicForm;
import org.igye.outline.htmlforms.ReorderParagraphChildren;
import org.igye.outline.model.Content;
import org.igye.outline.model.Image;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.SynopsisTopic;
import org.igye.outline.model.Text;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.igye.outline.selection.ActionType;
import org.igye.outline.selection.ObjectType;
import org.igye.outline.selection.Selection;
import org.igye.outline.selection.SelectionPart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.igye.outline.common.OutlineUtils.SQL_DEBUG_LOGGER_NAME;
import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.mapToSet;
import static org.igye.outline.common.OutlineUtils.toMap;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;
import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Component
public class Dao {
    private static final Logger DEBUG_LOG = LogManager.getLogger(SQL_DEBUG_LOGGER_NAME);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public UUID createImage(User requestor) {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        Image img = new Image();
        img.setOwner(session.load(User.class, requestor.getId()));
        return (UUID) session.save(img);
    }

    @Transactional
    public Optional<?> nextSibling(User requestor, UUID id, boolean toTheRight) {
        return getSibling(requestor, id, (list, comp) -> OutlineUtils.getNextSibling(list, comp, toTheRight));
    }

    @Transactional
    public Optional<?> furthestSibling(User requestor, UUID id, boolean toTheRight) {
        return getSibling(requestor, id, (list, comp) -> OutlineUtils.getFurthestSibling(list, comp, toTheRight));
    }

    @Transactional
    public Optional<?> firstChild(User requestor, UUID id) {
        Paragraph paragraph = loadParagraphByNotNullId(id, requestor);
        if (!paragraph.getChildParagraphs().isEmpty()) {
            return Optional.of(paragraph.getChildParagraphs().get(0));
        } else if (!paragraph.getTopics().isEmpty()) {
            return Optional.of(paragraph.getTopics().get(0));
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Paragraph> loadParent(User requestor, UUID id) {
        Object entity = loadEntityById(requestor, id);
        if (entity instanceof Paragraph) {
            return Optional.ofNullable(((Paragraph) entity).getParentParagraph());
        } else {
            return Optional.of(((Topic) entity).getParagraph());
        }
    }

    private <T> Optional<T> getSibling(User requestor, UUID id,
                                      F2<List<T>, Function<T,Boolean>, Optional<T>> getter) {
        Object entity = loadEntityById(requestor, id);
        if (entity instanceof Paragraph) {
            Paragraph par = (Paragraph) entity;
            if (par.getParentParagraph() == null) {
                return Optional.empty();
            } else {
                return getter.f(
                        (List<T>) par.getParentParagraph().getChildParagraphs(),
                        sib -> ((Paragraph)sib).getId().equals(par.getId())
                );
            }
        } else {
            Topic top = (Topic) entity;
            return getter.f(
                    (List<T>) top.getParagraph().getTopics(),
                    sib -> ((Topic)sib).getId().equals(top.getId())
            );
        }
    }

    @Transactional
    public Object loadEntityById(User requestor, UUID id) {
        List<Paragraph> paragraphs = queryParagraphByIdAndOwner(id, requestor).list();
        if (!paragraphs.isEmpty()) {
            return paragraphs.get(0);
        } else {
            return loadTopicById(id, requestor);
        }
    }

    @Transactional
    public UUID createSynopsisTopic(User requestor, EditSynopsisTopicForm request) {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        User owner = session.load(User.class, requestor.getId());
        Paragraph par = loadParagraphByNotNullId(request.getParentId(), requestor);
        SynopsisTopic topic = new SynopsisTopic();
        topic.setName(request.getName());
        topic.setOwner(owner);
        request.getContent().stream().forEach(contentForForm -> {
            if (TEXT.equals(contentForForm.getType())) {
                Text text = new Text();
                text.setText(contentForForm.getText());
                topic.addContent(text);
            } else if (IMAGE.equals(contentForForm.getType())) {
                Image image = session.load(Image.class, contentForForm.getId());
                if (!image.getOwner().getId().equals(requestor.getId())) {
                    throw new OutlineException("!image.getOwner().getId().equals(request.getId())");
                }
                topic.addContent(image);
            } else {
                throw new OutlineException("Unexpected type of content - '" + contentForForm.getType() + "'");
            }
        });
        topic.setParagraph(par);
        UUID id = (UUID) session.save(topic);
        par.addTopic(topic);
        return id;
    }

    @Transactional
    public void createParagraph(UUID parentId, String name) {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        Paragraph parent = session.load(Paragraph.class, parentId);
        Paragraph paragraph = new Paragraph();
        paragraph.setName(name);
        parent.addChildParagraph(paragraph);
    }

    @Transactional
    public void updateParagraph(User owner, UUID id, Consumer<Paragraph> updates) {
        Paragraph paragraph = loadParagraphByNotNullId(id, owner);
        updates.accept(paragraph);
    }

    @Transactional
    public void updateSynopsisTopic(User owner, EditSynopsisTopicForm form) {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        SynopsisTopic topic = loadSynopsisTopicByIdWithContent(form.getId(), owner);
        topic.setName(form.getName());
        Map<UUID, Content> oldContents = toMap(topic.getContents(), Content::getId);
        oldContents.values().forEach(topic::detachContentById);
        for (ContentForForm content : form.getContent()) {
            if (TEXT.equals(content.getType())) {
                if (content.getId() != null) {
                    Text text = (Text) oldContents.remove(content.getId());
                    text.setText(content.getText());
                    topic.addContent(text);
                } else {
                    Text text = new Text();
                    text.setText(content.getText());
                    topic.addContent(text);
                }
            } else if (IMAGE.equals(content.getType())) {
                if (content.getId() != null) {
                    UUID imgId = content.getId();
                    Content oldImg = oldContents.remove(imgId);
                    topic.addContent(oldImg != null ? oldImg : loadImageById(imgId, owner));
                } else {
                    throw new OutlineException("Unexpected condition:  image.getId() == null");
                }
            } else {
                throw new OutlineException("Unexpected type of content - '" + content.getType() + "'");
            }
        }
        oldContents.values().forEach(session::delete);
    }

    @Transactional
    public void reorderParagraphChildren(User owner, ReorderParagraphChildren request) {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        Paragraph parent = loadParagraphByNotNullId(request.getParentId(), owner);
        if (request.getParagraphs() != null) {
            List<Paragraph> paragraphs = parent.getChildParagraphs();
            Set<UUID> oldIdSet = mapToSet(paragraphs, Paragraph::getId);
            Set<UUID> newIdSet = ImmutableSet.copyOf(request.getParagraphs());
            if (!oldIdSet.equals(newIdSet)) {
                throw new OutlineException("!oldIdSet.equals(newIdSet) for paragraphs");
            }
            parent.setChildParagraphs(new LinkedList<>());
            for (UUID id : request.getParagraphs()) {
                parent.addChildParagraph(session.load(Paragraph.class, id));
            }
        }
        if (request.getTopics() != null) {
            List<Topic> topics = parent.getTopics();
            Set<UUID> oldIdSet = mapToSet(topics, Topic::getId);
            Set<UUID> newIdSet = ImmutableSet.copyOf(request.getTopics());
            if (!oldIdSet.equals(newIdSet)) {
                throw new OutlineException("!oldIdSet.equals(newIdSet) for topics");
            }
            parent.setTopics(new LinkedList<>());
            for (UUID id : request.getTopics()) {
                parent.addTopic(session.load(Topic.class, id));
            }
        }
    }

    @Transactional
    public void moveParagraph(User owner, UUID parToMoveId, UUID parToMoveToId) {
        Paragraph parToMove = loadParagraphByNotNullId(parToMoveId, owner);
        Paragraph parToMoveTo = loadParagraphByNotNullId(parToMoveToId, owner);
        Set<UUID> pathToRoot = new HashSet<>();
        Paragraph currPar = parToMoveTo;
        while(currPar != null) {
            pathToRoot.add(currPar.getId());
            currPar = currPar.getParentParagraph();
        }
        if (pathToRoot.contains(parToMove.getId())) {
            throw new OutlineException("pathToRoot.contains(parToMove.getId())");
        }
        Hibernate.initialize(parToMove.getParentParagraph().getChildParagraphs());
        Hibernate.initialize(parToMoveTo.getChildParagraphs());
        parToMove.getParentParagraph().getChildParagraphs().remove(parToMove);
        parToMoveTo.addChildParagraph(parToMove);
    }

    @Transactional
    public void moveTopic(User owner, UUID topicToMoveId, UUID parToMoveToId) {
        Topic topicToMove = loadTopicById(topicToMoveId, owner);
        Paragraph parToMoveTo = loadParagraphByNotNullId(parToMoveToId, owner);
        Hibernate.initialize(topicToMove.getParagraph().getTopics());
        Hibernate.initialize(parToMoveTo.getTopics());
        topicToMove.getParagraph().getTopics().remove(topicToMove);
        parToMoveTo.addTopic(topicToMove);
    }

    @Transactional
    public void moveImage(User owner, UUID imageToMoveId, UUID topicToMoveToId) {
        Image imgToMove = loadImageById(imageToMoveId, owner);
        SynopsisTopic srcTopic = imgToMove.getTopic();
        SynopsisTopic dstTopic = loadSynopsisTopicByIdWithContent(topicToMoveToId, owner);
        srcTopic.detachContentById(imgToMove);
        dstTopic.addContent(imgToMove);
    }

    @Transactional
    public void performActionOnSelectedObjects(User owner, Selection selection, UUID destId) {
        if (ActionType.MOVE.equals(selection.getActionType())) {
            for (SelectionPart selectionPart : selection.getSelections()) {
                if (ObjectType.PARAGRAPH.equals(selectionPart.getObjectType())) {
                    moveParagraph(owner, selectionPart.getSelectedId(), destId);
                } else if (ObjectType.TOPIC.equals(selectionPart.getObjectType())) {
                    moveTopic(owner, selectionPart.getSelectedId(), destId);
                } else if (ObjectType.IMAGE.equals(selectionPart.getObjectType())) {
                    moveImage(owner, selectionPart.getSelectedId(), destId);
                } else {
                    throw new OutlineException("Object type '" + selectionPart.getObjectType() + "' is not supported.");
                }
            }
        } else {
            throw new OutlineException("Action '" + selection.getActionType() + "' is not supported.");
        }
    }

    @Transactional
    public Paragraph loadParagraphById(Optional<UUID> idOpt, User requestor) {
        Paragraph paragraph = idOpt
                .map(id -> loadParagraphByNotNullId(id, requestor))
                .orElseGet(() -> loadRootParagraph(requestor));
        paragraph.getChildParagraphs().forEach(childPar -> {
            Hibernate.initialize(childPar.getChildParagraphs());
            Hibernate.initialize(childPar.getTopics());
        });
        Hibernate.initialize(paragraph.getTopics());
        Hibernate.initialize(paragraph.getTags());
        return paragraph;
    }

    @Transactional
    public Topic loadTopicById(UUID id, User owner) {
        return OutlineUtils.getCurrentSession(entityManager).createQuery(
                "from Topic where id = :id and owner = :owner", Topic.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner)
                .getSingleResult();
    }

    @Transactional
    public SynopsisTopic loadSynopsisTopicByIdWithContent(UUID id, User owner) {
        SynopsisTopic topic = (SynopsisTopic) loadTopicById(id, owner);
        Hibernate.initialize(topic.getContents());
        return topic;
    }

    @Transactional
    public Image loadImageById(UUID id, User owner) {
        return OutlineUtils.getCurrentSession(entityManager).createQuery(
                "from Image where id = :id and owner = :owner", Image.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner)
                .getSingleResult();
    }

    @Transactional
    public Optional<Topic> nextTopic(UUID currentTopicId, User owner) {
        return nextTopic(currentTopicId, owner, true);
    }

    @Transactional
    public Optional<Topic> prevTopic(UUID currentTopicId, User owner) {
        return nextTopic(currentTopicId, owner, false);
    }

    @Transactional(propagation = MANDATORY)
    public Optional<Topic> nextTopic(UUID currentTopicId, User owner, boolean direction) {
        Topic curTopic = loadTopicById(currentTopicId, owner);
        Paragraph paragraph = curTopic.getParagraph();
        List<Topic> topics = paragraph.getTopics();
        Optional<Topic> nextTopic = OutlineUtils.getNextSibling(
                topics, topic -> currentTopicId.equals(topic.getId()), direction
        );
        if (nextTopic.isPresent()) {
            return nextTopic;
        } else if (!isBook(paragraph)) {
            return findNextParagraphWithTopics(paragraph, direction)
                    .map(p -> {
                        if (direction) {
                            return p.getTopics().get(0);
                        } else {
                            return p.getTopics().get(p.getTopics().size()-1);
                        }
                    });
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Paragraph loadRootParagraph(User owner) {
        return OutlineUtils.getCurrentSession(entityManager).createQuery(
                "from Paragraph p where p.name = :name and p.owner = :owner and p.parentParagraph is null",
                Paragraph.class
        )
                .setParameter("name", ROOT_NAME)
                .setParameter("owner", owner)
                .setComment("@loadRootParagraph")
                .getSingleResult();
    }

    @Transactional(propagation = MANDATORY)
    public Paragraph loadParagraphByNotNullId(UUID id, User owner) {
        return queryParagraphByIdAndOwner(id, owner).getSingleResult();
    }

    @Transactional(propagation = MANDATORY)
    protected Optional<Paragraph> findNextParagraphWithTopics(Paragraph startParagraph, boolean direction) {
        Paragraph curPar = startParagraph.getParentParagraph();
        Optional<Tree<Paragraph>> parOpt = findNextNode(
                paragraph2Tree(curPar),
                new ArrayList<>(Arrays.asList(paragraph2Tree(startParagraph))),
                p -> !p.getOrig().getTopics().isEmpty(),
                direction
        );
        return parOpt.map(t -> t.getOrig());

    }

    private Query<Paragraph> queryParagraphByIdAndOwner(UUID id, User owner) {
        return OutlineUtils.getCurrentSession(entityManager).createQuery(
                "from Paragraph p where p.id = :id and owner = :owner", Paragraph.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner);
    }

    private Tree<Paragraph> paragraph2Tree(Paragraph paragraph) {
        return new Tree<Paragraph>() {
            @Override
            public List<Tree<Paragraph>> children() {
                return map(paragraph.getChildParagraphs(), Dao.this::paragraph2Tree);
            }

            @Override
            public Optional<Tree<Paragraph>> parent() {
                return paragraph.getParentParagraph() != null ?
                        Optional.of(paragraph2Tree(paragraph.getParentParagraph())) : Optional.empty();
            }

            @Override
            public boolean isRoot() {
                return isBook(paragraph);
            }

            @Override
            public boolean isEqual(Tree<Paragraph> other) {
                return paragraph.getId().equals(other.getOrig().getId());
            }

            @Override
            public Paragraph getOrig() {
                return paragraph;
            }
        };
    }

    interface Tree<T> {
        List<Tree<T>> children();
        Optional<Tree<T>> parent();
        boolean isRoot();
        boolean isEqual(Tree<T> other);
        T getOrig();
    }

    private <T> Optional<Tree<T>> findNextNode(Tree<T> curNode, List<Tree<T>> seen,
                                               Function<Tree<T>, Boolean> condition, boolean direction) {
        if (condition.apply(curNode)) {
            return Optional.of(curNode);
        } else {
            Optional<Tree> leftmostNotSeenOpt = findLeftmostNotSeen(curNode.children(), seen, direction);
            if (leftmostNotSeenOpt.isPresent()) {
                Tree leftmostNotSeen = leftmostNotSeenOpt.get();
                seen.add(leftmostNotSeen);
                return findNextNode(leftmostNotSeen, seen, condition, direction);
            } else {
                seen.add(curNode);
                if (!curNode.isRoot()) {
                    seen.add(curNode.parent().get());
                    return findNextNode(curNode.parent().get(), seen, condition, direction);
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private <T> Optional<Tree> findLeftmostNotSeen(List<Tree<T>> list, List<Tree<T>> seen, boolean direction) {
        Optional<Tree> res = Optional.empty();
        for (int i = (direction? list.size() - 1 : 0); (direction? i >= 0 : i < list.size()); i += (direction? -1 : 1)) {
            if (contains(seen, list.get(i))) {
                return res;
            } else {
                res = Optional.of(list.get(i));
            }
        }
        return res;
    }

    private <T> boolean contains(List<Tree<T>> seen, Tree<T> tree) {
        for (Tree t: seen) {
            if (t.isEqual(tree)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBook(Paragraph paragraph) {
        Paragraph parent = paragraph.getParentParagraph();
        return Paragraph.ROOT_NAME.equals(parent.getName()) && parent.getParentParagraph() == null;
    }
}
