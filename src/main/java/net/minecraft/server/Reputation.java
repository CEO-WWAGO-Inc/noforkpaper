package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList; // Paper
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reputation {

    private final Map<UUID, Reputation.a> a = Maps.newHashMap(); public Map<UUID, Reputation.a> getReputations() { return this.a; } // Paper - add getter for reputations

    public Reputation() {}

    public void b() {
        Iterator iterator = this.a.values().iterator();

        while (iterator.hasNext()) {
            Reputation.a reputation_a = (Reputation.a) iterator.next();

            reputation_a.a();
            if (reputation_a.b()) {
                iterator.remove();
            }
        }

    }

    private Stream<Reputation.b> c() {
        return this.a.entrySet().stream().flatMap((entry) -> {
            return ((Reputation.a) entry.getValue()).a((UUID) entry.getKey());
        });
    }

    // Paper start - Remove streams from reputation
    private List<Reputation.b> decompress() {
        List<Reputation.b> list = new ObjectArrayList<>();
        for (Map.Entry<UUID, Reputation.a> entry : getReputations().entrySet()) {
            for (Reputation.b cur : entry.getValue().decompress(entry.getKey())) {
                if (cur.a() != 0)
                    list.add(cur);
            }
        }
        return list;
    }
    // Paper end

    private Collection<Reputation.b> a(Random random, int i) {
        List<Reputation.b> list = decompress(); // Paper - Remove streams from reputation

        if (list.isEmpty()) {
            return Collections.emptyList();
        } else {
            int[] aint = new int[list.size()];
            int j = 0;

            for (int k = 0; k < list.size(); ++k) {
                Reputation.b reputation_b = (Reputation.b) list.get(k);

                j += Math.abs(reputation_b.a());
                aint[k] = j - 1;
            }

            Set<Reputation.b> set = Sets.newIdentityHashSet();

            for (int l = 0; l < i; ++l) {
                int i1 = random.nextInt(j);
                int j1 = Arrays.binarySearch(aint, i1);

                set.add(list.get(j1 < 0 ? -j1 - 1 : j1));
            }

            return set;
        }
    }

    private Reputation.a a(UUID uuid) {
        return (Reputation.a) this.a.computeIfAbsent(uuid, (uuid1) -> {
            return new Reputation.a();
        });
    }

    public void a(Reputation reputation, Random random, int i) {
        Collection<Reputation.b> collection = reputation.a(random, i);

        collection.forEach((reputation_b) -> {
            int j = reputation_b.c - reputation_b.b.j;

            if (j >= 2) {
                this.a(reputation_b.a).a.mergeInt(reputation_b.b, j, Reputation::a);
            }

        });
    }

    public int a(UUID uuid, Predicate<ReputationType> predicate) {
        Reputation.a reputation_a = (Reputation.a) this.a.get(uuid);

        return reputation_a != null ? reputation_a.a(predicate) : 0;
    }

    public void a(UUID uuid, ReputationType reputationtype, int i) {
        Reputation.a reputation_a = this.a(uuid);

        reputation_a.a.mergeInt(reputationtype, i, (integer, integer1) -> {
            return this.a(reputationtype, integer, integer1);
        });
        reputation_a.a(reputationtype);
        if (reputation_a.b()) {
            this.a.remove(uuid);
        }

    }

    public <T> Dynamic<T> a(DynamicOps<T> dynamicops) {
        return new Dynamic(dynamicops, dynamicops.createList(this.decompress().stream().map((reputation_b) -> {
            return reputation_b.a(dynamicops);
        }).map(Dynamic::getValue)));
    }

    public void a(Dynamic<?> dynamic) {
        dynamic.asStream().map(Reputation.b::a).flatMap((dataresult) -> {
            return SystemUtils.a(dataresult.result());
        }).forEach((reputation_b) -> {
            this.a(reputation_b.a).a.put(reputation_b.b, reputation_b.c);
        });
    }

    private static int a(int i, int j) {
        return Math.max(i, j);
    }

    private int a(ReputationType reputationtype, int i, int j) {
        int k = i + j;

        return k > reputationtype.h ? Math.max(reputationtype.h, i) : k;
    }

    public static class a { // Paper - make public

        private final Object2IntMap<ReputationType> a; private Object2IntMap<ReputationType> getEntries() { return a; } // Paper - OBFHELPER

        public a() { // Paper - make public - update CraftVillager setReputation on change
            this.a = new Object2IntOpenHashMap();
        }

        public int a(Predicate<ReputationType> predicate) {
            // Paper start - Remove streams from reputation
            int weight = 0;
            for (Object2IntMap.Entry<ReputationType> entry : getEntries().object2IntEntrySet()) {
                if (predicate.test(entry.getKey())) {
                    weight += entry.getIntValue() * entry.getKey().getWeight();
                }
            }
            return weight;
        }

        public List<Reputation.b> decompress(UUID uuid) {
            List<Reputation.b> list = new ObjectArrayList<>();
            for (Object2IntMap.Entry<ReputationType> entry : getEntries().object2IntEntrySet()) {
                list.add(new Reputation.b(uuid, entry.getKey(), entry.getIntValue()));
            }
            return list;
            // Paper - end
        }

        public Stream<Reputation.b> a(UUID uuid) {
            return this.a.object2IntEntrySet().stream().map((entry) -> {
                return new Reputation.b(uuid, (ReputationType) entry.getKey(), entry.getIntValue());
            });
        }

        public void a() {
            ObjectIterator objectiterator = this.a.object2IntEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<ReputationType> entry = (Entry) objectiterator.next();
                int i = entry.getIntValue() - ((ReputationType) entry.getKey()).i;

                if (i < 2) {
                    objectiterator.remove();
                } else {
                    entry.setValue(i);
                }
            }

        }

        public boolean b() {
            return this.a.isEmpty();
        }

        public void a(ReputationType reputationtype) {
            int i = this.a.getInt(reputationtype);

            if (i > reputationtype.h) {
                this.a.put(reputationtype, reputationtype.h);
            }

            if (i < 2) {
                this.b(reputationtype);
            }

        }

        public void b(ReputationType reputationtype) {
            this.a.removeInt(reputationtype);
        }

        // Paper start - Add villager reputation API
        private static final com.destroystokyo.paper.entity.villager.ReputationType[] REPUTATION_TYPES = com.destroystokyo.paper.entity.villager.ReputationType.values();
        public com.destroystokyo.paper.entity.villager.Reputation getPaperReputation() {
            int[] reputation = new int[REPUTATION_TYPES.length];
            reputation[com.destroystokyo.paper.entity.villager.ReputationType.MAJOR_NEGATIVE.ordinal()] = a.getOrDefault(ReputationType.MAJOR_NEGATIVE, 0);
            reputation[com.destroystokyo.paper.entity.villager.ReputationType.MAJOR_POSITIVE.ordinal()] = a.getOrDefault(ReputationType.MAJOR_POSITIVE, 0);
            reputation[com.destroystokyo.paper.entity.villager.ReputationType.MINOR_NEGATIVE.ordinal()] = a.getOrDefault(ReputationType.MINOR_NEGATIVE, 0);
            reputation[com.destroystokyo.paper.entity.villager.ReputationType.MINOR_POSITIVE.ordinal()] = a.getOrDefault(ReputationType.MINOR_POSITIVE, 0);
            reputation[com.destroystokyo.paper.entity.villager.ReputationType.TRADING.ordinal()] = a.getOrDefault(ReputationType.TRADING, 0);
            return com.destroystokyo.paper.entity.villager.ReputationConstructor.construct(reputation);
        }

        public void assignFromPaperReputation(com.destroystokyo.paper.entity.villager.Reputation rep) {
            int val;
            if ((val = rep.getReputation(com.destroystokyo.paper.entity.villager.ReputationType.MAJOR_NEGATIVE)) != 0) this.a.put(ReputationType.MAJOR_NEGATIVE, val);
            if ((val = rep.getReputation(com.destroystokyo.paper.entity.villager.ReputationType.MAJOR_POSITIVE)) != 0) this.a.put(ReputationType.MAJOR_POSITIVE, val);
            if ((val = rep.getReputation(com.destroystokyo.paper.entity.villager.ReputationType.MINOR_NEGATIVE)) != 0) this.a.put(ReputationType.MINOR_NEGATIVE, val);
            if ((val = rep.getReputation(com.destroystokyo.paper.entity.villager.ReputationType.MINOR_POSITIVE)) != 0) this.a.put(ReputationType.MINOR_POSITIVE, val);
            if ((val = rep.getReputation(com.destroystokyo.paper.entity.villager.ReputationType.TRADING)) != 0) this.a.put(ReputationType.TRADING, val);
        }
        // Paper end
    }

    static class b {

        public final UUID a;
        public final ReputationType b;
        public final int c;

        public b(UUID uuid, ReputationType reputationtype, int i) {
            this.a = uuid;
            this.b = reputationtype;
            this.c = i;
        }

        public int a() {
            return this.c * this.b.g;
        }

        public String toString() {
            return "GossipEntry{target=" + this.a + ", type=" + this.b + ", value=" + this.c + '}';
        }

        public <T> Dynamic<T> a(DynamicOps<T> dynamicops) {
            return new Dynamic(dynamicops, dynamicops.createMap(ImmutableMap.of(dynamicops.createString("Target"), MinecraftSerializableUUID.a.encodeStart(dynamicops, this.a).result().orElseThrow(RuntimeException::new), dynamicops.createString("Type"), dynamicops.createString(this.b.f), dynamicops.createString("Value"), dynamicops.createInt(this.c))));
        }

        public static DataResult<Reputation.b> a(Dynamic<?> dynamic) {
            return DataResult.unbox(DataResult.instance().group(dynamic.get("Target").read(MinecraftSerializableUUID.a), dynamic.get("Type").asString().map(ReputationType::a), dynamic.get("Value").asNumber().map(Number::intValue)).apply(DataResult.instance(), Reputation.b::new));
        }
    }
}
