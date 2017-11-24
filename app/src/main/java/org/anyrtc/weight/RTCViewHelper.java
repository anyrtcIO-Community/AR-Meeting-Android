package org.anyrtc.weight;

import org.webrtc.VideoRenderer;

/**
 * Created by Eric on 2016/7/26.
 */
public interface RTCViewHelper {
    /** Open main  Renderer
     * @param
     */
    public VideoRenderer OnRtcOpenLocalRender();

    /** Close main  Renderer
     */
    public void OnRtcRemoveLocalRender();


    /** Open  sub  Renderer
     * @param strRtcPeerId
     */

    public VideoRenderer OnRtcOpenRemoteRender(String strRtcPeerId);

    /** Close  sub  Renderer
     * @param strRtcPeerId
     */
    public void OnRtcRemoveRemoteRender(String strRtcPeerId);
}
