package Tool;

public class DemandCount {
	public String spoc;
	public int accepted		=	0;
	public int mapped		=	0;
	public int notMapped	=	0;
	public int rejected		=	0;
	public int grandTotal	=	0;
	
	public String getSpoc() {
		return spoc;
	}
	public void setSpoc(String spoc) {
		this.spoc = spoc;
		setGrandTotal();
	}
	public int getAccepted() {
		return accepted;
	}
	public void setAccepted(int accepted) {
		this.accepted = accepted;
		setGrandTotal();
	}
	public int getMapped() {
		return mapped;
	}
	public void setMapped(int mapped) {
		this.mapped = mapped;
		setGrandTotal();
	}
	public int getNotMapped() {
		return notMapped;
	}
	public void setNotMapped(int notMapped) {
		this.notMapped = notMapped;
		setGrandTotal();
	}
	public int getRejected() {
		return rejected;
	}
	public void setRejected(int rejected) {
		this.rejected = rejected;
		setGrandTotal();
	}
	public int getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal() {
		this.grandTotal = accepted + mapped + notMapped + rejected;
	}
}
