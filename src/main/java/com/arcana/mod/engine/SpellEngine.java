package com.arcana.mod.engine;
import com.arcana.mod.card.Card;
import java.util.Stack;


public class SpellEngine {
    // TODO: Implement the spell execution logic here
    // TODO: Implement ExecutionContext class to hold the context for spell execution
    
    public static boolean execute(ExecutionContext context) { // Returns false if wand should experience failure, true if spell executed successfully
        // Implement the logic to execute the spell based on the provided context
        // This could involve checking the spell type, applying effects, etc.
        Stack<Card> modStack = new Stack<Card>(); // A place to store cards that will support other cards during execution, mainly modifier cards
        Integer EffectCredits = 1; // A counter to keep track of the number of effect cards that can be executed in the current execution. This allows for the execution of multiple effect cards in a single spell execution, as long as the total number of effect cards does not exceed the available EffectCredits.
        Integer ExecutionLimit = 1000; // A limit to prevent infinite loops or excessive execution time. This can be adjusted based on the expected complexity of spells. Is reduced by 1 for each card executed, and if it reaches 0, the spell execution will stop to prevent potential infinite loops or excessive execution time, resulting in wand failure.
        
        if (context.hasCards()) {
            // Execute the spell using the cards in the context
            // For example, apply effects based on the cards

            while (context.hasCards() && ExecutionLimit > 0) {
                Card currentCard = context.getCurrentCard();
                if (currentCard != null) {
                    // Execute the current card's effect
                    boolean success = currentCard.execute(context, modStack, EffectCredits); // TODO: change provided context in card class (Task for Dario)
                    if (!success) {
                        // Handle failure of card execution, possibly due to insufficient EffectCredits
                        return false; // Indicate failure due to card execution failure
                    }
                    // Move focus to the next card based on the execution order
                    context.moveFocus(1);
                    ExecutionLimit--; // Decrease the execution limit
                } else {
                    // If the current card is null, break the loop to avoid infinite execution
                    break;
                }
            }
            return true; // Indicate success
        } else {
            // Handle the case where there are no cards in the context
            return false; // Indicate failure due to no cards
        }
    }
}
