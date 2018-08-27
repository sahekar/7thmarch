package com.dvsts.avaya.processing.logic.mos;

import lombok.Data;

import java.io.PrintWriter;

@Data
public class MosDto {
    protected long					_jitterDelay;
    public long					_roundTripDelay;
    public double				_packageLoss;

    public double				_R_;
    public double				_Ro_;
    public double				_No_;
    public double				_Nos_;
    public double				_Nor_;
    public double				_Nfo_;
    public double				_Pre_;
    public double				_Is_;
    public double				_IoIr_;
    public double				_Ist_;
    public double				_Iq_;
    public double				_Id_;
    public double				_Ie_eff_;
    public double				_Idte_;
    public double				_Idtes_		= -1;
    public double				_Idle_;
    public double				_Idd_;

    public double				_Y_, _Z_, _G_, _Q_, _OLR_;
    public double				_STMRo_, _XoIr_, _Roe_, _Re_, _Rle_, _TERVs_;
    public boolean				_debugPrint	= false;
    public PrintWriter _fileWriter;
    public int					_codec;
}
