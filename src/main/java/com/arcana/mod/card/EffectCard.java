public class EffectCard extends Card {

    public EffectCard(
            Properties properties,
            Enum<Type> cardType,
            String cardName,
            String cardDescription,
            int textureIndex,
            String effectDescription
    ) {
        super(properties, cardType, cardName, cardDescription, textureIndex);
    }

    public String execute() {
        return "Effect Card executed: " + getCardName();
    }
}