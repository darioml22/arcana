public class Card extends Item {

    private final Type cardType;
    private final String cardName;
    private final String cardDescription;
    private final int textureIndex; //not sure how this works


    public CardItem(
            Properties properties,
            Enum<Type> cardType,
            String cardName,
            String cardDescription,
            int textureIndex
    ) {
        super(properties);

        this.cardType = cardType;
        this.cardName = cardName;
        this.cardDescription = cardDescription;
        this.textureIndex = textureIndex;
    }  

    public String execute() {
        
    }
    
    public String getCardType() {
        return cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public String getDescription() {
        return cardDescription;
    }

    public int getTextureIndex() {
        return textureIndex;
    }
}