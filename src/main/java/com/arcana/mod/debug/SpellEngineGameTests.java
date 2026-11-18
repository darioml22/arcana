package com.arcana.mod.debug;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.card.Card;
import com.arcana.mod.engine.CastResult;
import com.arcana.mod.engine.ExecutionContext;
import com.arcana.mod.engine.SpellEngine;
import com.arcana.mod.registry.ModCards;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Game tests for the spell engine, reproducing the "Spell Using Examples" document with the
// registered cards. Run with ./gradlew runGameTestServer.
public final class SpellEngineGameTests {

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, Arcanamod.MODID);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENGINE_EXAMPLES =
            TEST_FUNCTIONS.register("spell_engine_examples", () -> SpellEngineGameTests::engineExamples);

    private SpellEngineGameTests() {}

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(Arcanamod.MODID, "default"),
                new TestEnvironmentDefinition.AllOf(List.of()));
        event.registerTest(
                Identifier.fromNamespaceAndPath(Arcanamod.MODID, "spell_engine_examples"),
                new FunctionGameTestInstance(ENGINE_EXAMPLES.getKey(),
                        new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 100, 0, true)));
    }

    // ---------- Test body ----------

    private static void engineExamples(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        Card fire = ModCards.FIRE.get();
        Card heavy = ModCards.HEAVY.get();
        Card projectile = ModCards.PROJECTILE.get();
        Card explosion = ModCards.EXPLOSION.get();
        Card skip = ModCards.SKIP.get();
        Card restart = ModCards.RESTART.get();
        Card backwards = ModCards.BACKWARDS.get();
        Card highCost = ModCards.CAST_IF_HIGH_COST.get();
        Card onFire = ModCards.CAST_IF_ON_FIRE.get();

        // Basic example: Fire and Heavy go to the stack, Projectile fires with both
        Wand basic = new Wand(helper, caster, 100F, fire, heavy, projectile);
        basic.castExpect(CastResult.CAST_END_OF_WAND, 0, false, 70F);

        // Empty slot example: the empty slot is ignored
        Wand empty = new Wand(helper, caster, 100F, fire, null, projectile);
        empty.castExpect(CastResult.CAST_END_OF_WAND, 0, false, 85F);

        // Advanced example 1 (Heavy instead of Accelerating shot): Heavy, CIHC false -> Skip skipped,
        // Restart, Heavy, CIHC true -> Skip skips Restart, Fire, Projectile. Paid 15+5+15+10+5 = 50
        Wand logic = new Wand(helper, caster, 60F, heavy, highCost, skip, restart, fire, projectile);
        logic.castExpect(CastResult.CAST_END_OF_WAND, 0, false, 10F);

        // Skip + Backwards, resumed over several casts (focus/direction/mana persisted between them)
        Wand back = new Wand(helper, caster, 100F, skip, explosion, heavy, projectile, backwards);
        back.castExpect(CastResult.CAST, 4, false, 80F);          // Skip skips Explosion, Heavy + Projectile
        back.castExpect(CastResult.CAST, 2, true, 70F);           // Backwards, Projectile again
        back.castExpect(CastResult.CAST, 0, true, 30F);           // Heavy (unsupported) + Explosion
        back.castExpect(CastResult.END_OF_WAND, 0, false, 30F);   // Skip has nothing to skip, end of wand

        // Cast if on fire: skips Explosion while not burning, casts it while burning
        Wand burning = new Wand(helper, caster, 100F, onFire, explosion, projectile);
        burning.castExpect(CastResult.CAST_END_OF_WAND, 0, false, 95F);
        caster.setRemainingFireTicks(100);
        burning.castExpect(CastResult.CAST, 2, false, 70F);
        caster.setRemainingFireTicks(0);

        // Infinite loop paid with mana: Heavy+Restart costs 20 per lap, the 6th Heavy can't be paid
        Wand drain = new Wand(helper, caster, 100F, heavy, restart);
        drain.castExpect(CastResult.FAILURE_NO_MANA, 0, false, 0F);

        // Infinite loop with plenty of mana: stopped by the execution limit
        Wand endless = new Wand(helper, caster, 1_000_000F, restart);
        endless.castExpect(CastResult.FAILURE_EXECUTION_LIMIT, 0, false, 0F);

        // Modifiers without an effect: discarded at the end, nothing paid
        Wand noEffect = new Wand(helper, caster, 100F, fire);
        noEffect.castExpect(CastResult.END_OF_WAND, 0, false, 100F);

        // Not enough mana for the whole spell block
        Wand poor = new Wand(helper, caster, 100F, fire, projectile);
        poor.mana = 12F;
        poor.castExpect(CastResult.FAILURE_NO_MANA, 0, false, 0F);

        // No cards at all
        Wand nothing = new Wand(helper, caster, 100F, null, null);
        nothing.castExpect(CastResult.EMPTY, 0, false, 100F);

        helper.succeed();
    }

    // Mimics what WandItem persists between uses: focus, direction and mana.
    private static final class Wand {
        private final GameTestHelper helper;
        private final Player caster;
        private final List<Card> cards;
        private final float maxMana;
        private float mana;
        private int focus = 0;
        private boolean reversed = false;
        private int castNumber = 0;

        Wand(GameTestHelper helper, Player caster, float maxMana, Card... cards) {
            this.helper = helper;
            this.caster = caster;
            this.cards = Arrays.asList(cards);
            this.maxMana = maxMana;
            this.mana = maxMana;
        }

        void castExpect(CastResult expected, int expectedFocus, boolean expectedReversed, float expectedMana) {
            castNumber++;
            ExecutionContext ctx = new ExecutionContext(cards, helper.getLevel(), caster, maxMana, mana, focus, reversed);
            CastResult result = SpellEngine.cast(ctx);
            focus = ctx.getFocus();
            reversed = ctx.isReversed();
            mana = ctx.getMana();

            String where = describe() + " cast #" + castNumber + ": ";
            String trace = " | trace: " + String.join(" / ", ctx.getTrace());
            helper.assertTrue(result == expected, where + "expected " + expected + " but got " + result + trace);
            helper.assertTrue(focus == expectedFocus, where + "expected focus " + expectedFocus + " but got " + focus + trace);
            helper.assertTrue(reversed == expectedReversed, where + "expected reversed=" + expectedReversed + trace);
            helper.assertTrue(Math.abs(mana - expectedMana) < 0.01F, where + "expected mana " + expectedMana + " but got " + mana + trace);

            Arcanamod.LOGGER.info("[spell test] {}{} -> focus {}{}, mana {}", where, result, focus,
                    reversed ? " (reversed)" : "", Card.formatMana(mana));
            for (String line : ctx.getTrace()) {
                Arcanamod.LOGGER.info("[spell test]     {}", line);
            }
        }

        private String describe() {
            return cards.stream().map(c -> c == null ? "empty" : c.getCardName()).toList().toString();
        }
    }
}
