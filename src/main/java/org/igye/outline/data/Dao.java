package org.igye.outline.data;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ReorderParagraphChildren;
import org.igye.outline.model.*;
import org.igye.outline.selection.ActionType;
import org.igye.outline.selection.ObjectType;
import org.igye.outline.selection.Selection;
import org.igye.outline.selection.SelectionPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.igye.outline.common.OutlineUtils.SQL_DEBUG_LOGGER_NAME;
import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Component
public class Dao {
    private static final Logger DEBUG_LOG = LogManager.getLogger(SQL_DEBUG_LOGGER_NAME);

    @Autowired
    private SessionFactory sessionFactory;

    @Transactional
    public void createParagraph(UUID parentId, String name) {
        Session session = sessionFactory.getCurrentSession();
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
    public void reorderParagraphChildren(User owner, ReorderParagraphChildren request) {
        Session session = sessionFactory.getCurrentSession();
        Paragraph parent = loadParagraphByNotNullId(request.getParentId(), owner);
        if (request.getParagraphs() != null) {
            List<Paragraph> paragraphs = parent.getChildParagraphs();
            Set<UUID> oldIdSet = paragraphs.stream().map(p -> p.getId()).collect(Collectors.toSet());
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
            Set<UUID> oldIdSet = topics.stream().map(p -> p.getId()).collect(Collectors.toSet());
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
    public void performActionOnSelectedObjects(User owner, Selection selection, UUID destParId) {
        if (ActionType.MOVE.equals(selection.getActionType())) {
            for (SelectionPart selectionPart : selection.getSelections()) {
                if (ObjectType.PARAGRAPH.equals(selectionPart.getObjectType())) {
                    moveParagraph(owner, selectionPart.getSelectedId(), destParId);
                } else if (ObjectType.TOPIC.equals(selectionPart.getObjectType())) {
                    moveTopic(owner, selectionPart.getSelectedId(), destParId);
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
        Hibernate.initialize(paragraph.getChildParagraphs());
        Hibernate.initialize(paragraph.getTopics());
        Hibernate.initialize(paragraph.getTags());
        return paragraph;
    }

    @Transactional
    public Topic loadTopicById(UUID id, User owner) {
        return sessionFactory.getCurrentSession().createQuery(
                "from Topic where id = :id and owner = :owner", Topic.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner)
                .getSingleResult();
    }

    @Transactional
    public Topic loadSynopsisTopicByIdWithContent(UUID id, User owner) {
        SynopsisTopic topic = (SynopsisTopic) loadTopicById(id, owner);
        Hibernate.initialize(topic.getContents());
        return topic;
    }

    @Transactional
    public Image loadImageById(UUID id, User owner) {
        return sessionFactory.getCurrentSession().createQuery(
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
        Optional<Topic> nextTopic = Optional.empty();
        for (int i = 0; i < topics.size(); i++) {
            if (currentTopicId.equals(topics.get(i).getId()) && (direction ? i < topics.size() - 1 : i > 0)) {
                nextTopic = Optional.of(topics.get(i + (direction ? 1 : -1)));
                break;
            }
        }
        if (nextTopic.isPresent()) {
            return nextTopic;
        } else if (!isBook(curTopic.getParagraph())) {
            return findNextParagraphWithTopics(curTopic.getParagraph() ,direction)
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
        return sessionFactory.getCurrentSession().createQuery(
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
        return sessionFactory.getCurrentSession().createQuery(
                "from Paragraph p where p.id = :id and owner = :owner", Paragraph.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner)
                .getSingleResult();
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

    private Tree<Paragraph> paragraph2Tree(Paragraph paragraph) {
        return new Tree<Paragraph>() {
            @Override
            public List<Tree<Paragraph>> children() {
                return paragraph.getChildParagraphs().stream()
                        .map(p -> paragraph2Tree(p))
                        .collect(Collectors.toList());
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
