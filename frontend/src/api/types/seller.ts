export interface Seller {
  sellerId: number;
  userId: number;
  sellerNumber: string;
  sellerGender: string;
  sellerAge: number | null;
  sellerSchool: string;
  sellerAddress: string;
  sellerBirthday: string;
  briefIntroduction: string;
  examineState: string;
  creditScore: number | null;
  createTime: string;
  updateTime: string;
}

export interface UpdateSellerParams {
  sellerGender?: string;
  sellerAge?: number;
  sellerSchool?: string;
  sellerAddress?: string;
  sellerBirthday?: string;
  briefIntroduction?: string;
}
