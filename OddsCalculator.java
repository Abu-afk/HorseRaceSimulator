import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates betting odds for horses based on their performance, track conditions,
 * and recent betting patterns.
 */
public class OddsCalculator {
    private static final double MIN_ODDS = 1.1;  // Minimum odds (almost certain win)
    private static final double MAX_ODDS = 50.0; // Maximum odds (very unlikely win)
    private static final double BETTING_PATTERN_WEIGHT = 0.3; // How much betting patterns affect odds
    
    /**
     * Calculate odds for a list of horses
     * 
     * @param horses The list of horses to calculate odds for
     * @param track The track they will be racing on
     * @param bettingHistory The betting history to consider for betting patterns
     * @return Map of horse to odds
     */
    public Map<Horse, Double> calculateOdds(List<Horse> horses, Track track, BettingHistory bettingHistory) {
        Map<Horse, Double> odds = new HashMap<>();
        
        // Base odds calculation using horse attributes and track conditions
        Map<Horse, Double> baseOdds = calculateBaseOdds(horses, track);
        
        // Adjust odds based on betting patterns
        Map<Horse, Double> adjustedOdds = adjustForBettingPatterns(baseOdds, bettingHistory);
        
        return adjustedOdds;
    }
    
    /**
     * Calculate base odds for horses based on their attributes and track conditions
     * 
     * @param horses The list of horses
     * @param track The track
     * @return Map of horse to base odds
     */
    private Map<Horse, Double> calculateBaseOdds(List<Horse> horses, Track track) {
        Map<Horse, Double> baseOdds = new HashMap<>();
        Map<Horse, Double> winProbabilities = calculateWinProbabilities(horses, track);
        
        // Convert probabilities to odds (odds = 1/probability)
        for (Horse horse : winProbabilities.keySet()) {
            double probability = winProbabilities.get(horse);
            // Apply small random variation to make odds more interesting
            double variation = 0.9 + (Math.random() * 0.2); // 0.9 to 1.1
            double calculatedOdds = (1.0 / probability) * variation;
            
            // Ensure odds are within acceptable range
            double finalOdds = Math.max(MIN_ODDS, Math.min(MAX_ODDS, calculatedOdds));
            
            // Round to one decimal place for cleaner display
            finalOdds = Math.round(finalOdds * 10) / 10.0;
            
            baseOdds.put(horse, finalOdds);
        }
        
        return baseOdds;
    }
    
    /**
     * Calculate the probability of each horse winning based on attributes and track
     * 
     * @param horses The list of horses
     * @param track The track
     * @return Map of horse to win probability (values sum to 1.0)
     */
    private Map<Horse, Double> calculateWinProbabilities(List<Horse> horses, Track track) {
        Map<Horse, Double> scores = new HashMap<>();
        double totalScore = 0.0;
        
        for (Horse horse : horses) {
            // Calculate a score based on horse attributes
            double score = calculateHorseScore(horse, track);
            scores.put(horse, score);
            totalScore += score;
        }
        
        // Normalize scores to probabilities (sum to 1.0)
        Map<Horse, Double> probabilities = new HashMap<>();
        for (Horse horse : horses) {
            double probability = totalScore > 0 ? scores.get(horse) / totalScore : 1.0 / horses.size();
            probabilities.put(horse, probability);
        }
        
        return probabilities;
    }
    
    /**
     * Calculate a score for a horse based on its attributes and track conditions
     * 
     * @param horse The horse
     * @param track The track
     * @return A score representing the horse's chance of winning
     */
    private double calculateHorseScore(Horse horse, Track track) {
        double baseSpeed = horse.getBaseSpeed();
        double confidence = horse.getConfidence();
        double turnHandling = horse.getTurnHandling();
        double stamina = horse.getStamina();
        
        // Get track condition effect
        TrackCondition condition = track.getCondition();
        double conditionEffect = getTrackConditionEffect(horse, condition);
        
        // Calculate curve factor effect based on turn handling
        double curveFactor = 0.0;
        if (track instanceof OvalTrack) {
            curveFactor = 0.8; // Moderate curves
        } else if (track instanceof FigureEightTrack) {
            curveFactor = 0.6; // Sharp curves
        } else if (track instanceof ZigzagTrack) {
            curveFactor = 0.4; // Very sharp turns
        }
        
        double curveHandling = 1.0 - ((1.0 - curveFactor) * (1.0 - turnHandling));
        
        // Calculate breed advantage
        double breedAdvantage = getBreedAdvantage(horse.getBreed());
        
        // Calculate equipment advantage
        double equipmentAdvantage = getEquipmentAdvantage(horse.getEquipment(), track);
        
        // Calculate overall score
        double score = baseSpeed * confidence * curveHandling * conditionEffect * 
                       stamina * breedAdvantage * equipmentAdvantage;
        
        return score;
    }
    
    /**
     * Get the effect of track conditions on a horse's performance
     * 
     * @param horse The horse
     * @param condition The track condition
     * @return Multiplier for the horse's score
     */
    private double getTrackConditionEffect(Horse horse, TrackCondition condition) {
        double baseEffect = condition.getSpeedFactor();
        
        // Some breeds perform better in certain conditions
        HorseBreed breed = horse.getBreed();
        if (condition == TrackCondition.MUDDY && breed == HorseBreed.ARABIAN) {
            baseEffect += 0.1; // Arabs do better in mud
        } else if (condition == TrackCondition.ICY && breed == HorseBreed.CLYDESDALE) {
            baseEffect += 0.15; // Clydesdales do better on ice
        } else if (condition == TrackCondition.WINDY && breed == HorseBreed.THOROUGHBRED) {
            baseEffect += 0.05; // Thoroughbreds do better in wind
        }
        
        // Horse equipment affects performance in conditions
        HorseEquipment equipment = horse.getEquipment();
        if (condition == TrackCondition.MUDDY && 
            equipment.getHorseshoes() == HorseEquipment.HorseshoeType.TRACTION) {
            baseEffect += 0.1; // Traction horseshoes help in mud
        } else if (condition == TrackCondition.WINDY && 
                  equipment.getAccessory() == HorseEquipment.AccessoryType.BLINDERS) {
            baseEffect += 0.05; // Blinders help in wind
        }
        
        return baseEffect;
    }
    
    /**
     * Get the advantage factor for a horse breed
     * 
     * @param breed The horse breed
     * @return Multiplier for the horse's score
     */
    private double getBreedAdvantage(HorseBreed breed) {
        if (breed == HorseBreed.THOROUGHBRED) {
            return 1.2; // Thoroughbreds are bred for racing
        } else if (breed == HorseBreed.QUARTER_HORSE) {
            return 1.15; // Quarter horses are fast sprinters
        } else if (breed == HorseBreed.ARABIAN) {
            return 1.1; // Arabians have good endurance
        } else if (breed == HorseBreed.STANDARDBRED) {
            return 1.05; // Good all-around racers
        }
        return 1.0; // Default for other breeds
    }
    
    /**
     * Get the advantage factor for horse equipment on a specific track
     * 
     * @param equipment The horse equipment
     * @param track The track
     * @return Multiplier for the horse's score
     */
    private double getEquipmentAdvantage(HorseEquipment equipment, Track track) {
        double advantage = 1.0;
        
        // Saddle effects
        if (equipment.getSaddle() == HorseEquipment.SaddleType.RACING) {
            advantage *= 1.1; // Racing saddles are best for racing
        } else if (equipment.getSaddle() == HorseEquipment.SaddleType.WESTERN && 
                  track instanceof ZigzagTrack) {
            advantage *= 1.05; // Western saddles help with quick turns
        }
        
        // Horseshoe effects
        if (equipment.getHorseshoes() == HorseEquipment.HorseshoeType.LIGHTWEIGHT) {
            advantage *= 1.08; // Lightweight horseshoes increase speed
        } else if (equipment.getHorseshoes() == HorseEquipment.HorseshoeType.TRACTION && 
                  (track instanceof FigureEightTrack || track instanceof ZigzagTrack)) {
            advantage *= 1.05; // Traction helps with sharp turns
        }
        
        // Accessory effects
        if (equipment.getAccessory() == HorseEquipment.AccessoryType.BLINDERS) {
            advantage *= 1.03; // Blinders help focus
        } else if (equipment.getAccessory() == HorseEquipment.AccessoryType.LUCKY_CHARM) {
            advantage *= 1.01; // Slight "lucky" advantage
        }
        
        return advantage;
    }
    
    /**
     * Adjust odds based on betting patterns
     * 
     * @param baseOdds The calculated base odds
     * @param bettingHistory The betting history to analyze for patterns
     * @return Adjusted odds
     */
    private Map<Horse, Double> adjustForBettingPatterns(Map<Horse, Double> baseOdds, BettingHistory bettingHistory) {
        Map<Horse, Double> adjustedOdds = new HashMap<>(baseOdds);
        
        // Get the total bet amount on each horse
        Map<Horse, Double> betAmounts = new HashMap<>();
        double totalBetAmount = 0.0;
        
        for (Horse horse : baseOdds.keySet()) {
            double betAmount = bettingHistory.getTotalBetOnHorse(horse);
            betAmounts.put(horse, betAmount);
            totalBetAmount += betAmount;
        }
        
        // If no bets have been placed, return base odds
        if (totalBetAmount <= 0) {
            return baseOdds;
        }
        
        // Adjust odds based on betting distribution
        for (Horse horse : baseOdds.keySet()) {
            double baseOdd = baseOdds.get(horse);
            double betPercentage = betAmounts.get(horse) / totalBetAmount;
            
            // As more bets go to a horse, its odds decrease (favorite effect)
            // The adjustment is proportional to the bet percentage, but capped
            double adjustmentFactor = 1.0 - (betPercentage * BETTING_PATTERN_WEIGHT);
            double adjustedOdd = baseOdd * adjustmentFactor;
            
            // Ensure odds remain within acceptable range
            adjustedOdd = Math.max(MIN_ODDS, Math.min(MAX_ODDS, adjustedOdd));
            
            // Round to one decimal place
            adjustedOdd = Math.round(adjustedOdd * 10) / 10.0;
            
            adjustedOdds.put(horse, adjustedOdd);
        }
        
        return adjustedOdds;
    }
}