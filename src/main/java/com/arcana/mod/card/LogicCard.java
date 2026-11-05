public class LogicCard extends Card {

    public LogicCard(
            Properties properties,
            int spellId,
            Enum<Type> cardType,
            String cardName,
            String cardDescription,
            int textureIndex
    ) {
        super(properties, cardType, cardName, cardDescription, textureIndex);
    }

    public String execute() {
        return "Logic Card executed: " + getCardName();
    }
}