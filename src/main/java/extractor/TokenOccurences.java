package extractor;

import java.util.ArrayList;
import java.util.List;

public class TokenOccurences {
	
	private final List<TokenOccurenceContext> occurenceList;

	public TokenOccurences() {
		this.occurenceList = new ArrayList<TokenOccurenceContext>();;
	}
	
	public void addOccurence(TokenOccurenceContext occurence) {
		occurenceList.add(occurence);
	}
	
	public List<TokenOccurenceContext> getList(){
		return List.copyOf(occurenceList);
	}
}
