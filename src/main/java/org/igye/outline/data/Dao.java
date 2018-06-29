package org.igye.outline.data;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Component
public class Dao {
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
                "from Topic t where t.id = :id and owner = :owner", Topic.class
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
