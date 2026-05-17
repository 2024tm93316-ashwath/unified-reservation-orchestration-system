import { useState, useEffect, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import TopBar from '../../components/common/TopBar';
import { useAuth } from '../../context/AuthContext';
import {
  getResources, getTimeSlotsByResource, getSeatsByResource,
  getQuotasByResource,
} from '../../api/resourceApi';
import {
  checkAvailability, createReservation, confirmReservation,
} from '../../api/reservationApi';
import { typeLabel, typeIcon, typeColor, typeBg, formatDateTime } from '../../utils/formatters';
import { CheckCircle2, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

const STEPS = ['Select Resource', 'Check Availability', 'Hold', 'Confirm'];

export default function BookingFlow() {
  const { user } = useAuth();
  const { state } = useLocation();
  const navigate = useNavigate();

  const [step, setStep] = useState(0);
  const [resources, setResources] = useState([]);
  const [selectedResource, setSelectedResource] = useState(state?.resource ?? null);

  // Availability inputs
  const [timeSlots, setTimeSlots] = useState([]);
  const [seats, setSeats] = useState([]);
  const [quotas, setQuotas] = useState([]);
  const [selectedSlotId, setSelectedSlotId] = useState(null);
  const [selectedSeatId, setSelectedSeatId] = useState(null);
  const [selectedQuotaId, setSelectedQuotaId] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [availResult, setAvailResult] = useState(null);

  // Reservation
  const [reservation, setReservation] = useState(null);
  const [confirming, setConfirming] = useState(false);
  const [holding, setHolding] = useState(false);

  const [holdTimer, setHoldTimer] = useState(null);
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    getResources().then((r) => setResources(r.data?.data ?? r.data ?? []));
  }, []);

  useEffect(() => {
    if (state?.resource) { setSelectedResource(state.resource); setStep(1); }
  }, [state]);

  useEffect(() => {
    if (!selectedResource) return;
    const type = selectedResource.reservationType;
    if (type === 'TIME_BASED') getTimeSlotsByResource(selectedResource.id).then((r) => setTimeSlots(r.data?.data ?? r.data ?? []));
    if (type === 'SEAT_BASED') getSeatsByResource(selectedResource.id).then((r) => setSeats(r.data?.data ?? r.data ?? []));
    if (type === 'QUOTA_BASED') getQuotasByResource(selectedResource.id).then((r) => setQuotas(r.data?.data ?? r.data ?? []));
  }, [selectedResource]);

  // Countdown timer
  useEffect(() => {
    if (!reservation?.holdExpiresAt) return;
    const interval = setInterval(() => {
      const diff = new Date(reservation.holdExpiresAt) - Date.now();
      if (diff <= 0) { setTimeLeft('Expired'); clearInterval(interval); }
      else {
        const m = Math.floor(diff / 60000);
        const s = Math.floor((diff % 60000) / 1000);
        setTimeLeft(`${m}m ${s.toString().padStart(2, '0')}s`);
      }
    }, 1000);
    setHoldTimer(interval);
    return () => clearInterval(interval);
  }, [reservation]);

  const resType = selectedResource?.reservationType;

  const handleCheckAvailability = async () => {
    const payload = { resourceId: selectedResource.id };
    if (resType === 'TIME_BASED') payload.timeSlotId = selectedSlotId;
    if (resType === 'SEAT_BASED') payload.seatMapId = selectedSeatId;
    if (resType === 'QUOTA_BASED') payload.quotaDefinitionId = selectedQuotaId;
    if (resType === 'RESOURCE_BASED') { payload.startTime = startDate; payload.endTime = endDate; }
    if (resType === 'CAPACITY_BASED') { payload.quantity = quantity; }
    try {
      const r = await checkAvailability(payload);
      setAvailResult(r.data?.data ?? r.data);
      setStep(2);
    } catch (e) { toast.error(e.message); }
  };

  const handleHold = async () => {
    setHolding(true);
    const payload = {
      resourceId: selectedResource.id,
      userId: user.userId,
      reservationType: resType,
    };
    if (resType === 'TIME_BASED') payload.timeSlotId = selectedSlotId;
    if (resType === 'SEAT_BASED') payload.seatMapId = selectedSeatId;
    if (resType === 'QUOTA_BASED') payload.quotaDefinitionId = selectedQuotaId;
    if (resType === 'RESOURCE_BASED') { payload.startTime = startDate; payload.endTime = endDate; }
    if (resType === 'CAPACITY_BASED') payload.quantity = quantity;
    try {
      const r = await createReservation(payload);
      setReservation(r.data?.data ?? r.data);
      toast.success('Resource held! Please confirm within the time limit.');
      setStep(3);
    } catch (e) { toast.error(e.message); }
    finally { setHolding(false); }
  };

  const handleConfirm = async () => {
    setConfirming(true);
    try {
      await confirmReservation(reservation.id);
      if (holdTimer) clearInterval(holdTimer);
      toast.success('Booking confirmed! 🎉');
      navigate('/user/reservations');
    } catch (e) { toast.error(e.message); }
    finally { setConfirming(false); }
  };

  return (
    <div className="page-wrapper">
      <TopBar title="Book a Resource" subtitle="Follow the steps to complete your reservation" />
      <div className="app-content animate-fade-in">
        <div className="booking-flow">
          {/* Step indicator */}
          <div className="steps">
            {STEPS.map((label, i) => (
              <div className="step" key={i}>
                <div className={`step__num step__num--${i < step ? 'done' : i === step ? 'active' : 'pending'}`}>
                  {i < step ? '✓' : i + 1}
                </div>
                <span className={`step__label${i === step ? ' step__label--active' : ''}`}>{label}</span>
                {i < STEPS.length - 1 && <div className={`step__line${i < step ? ' step__line--done' : ''}`} />}
              </div>
            ))}
          </div>

          {/* Step 0: Select Resource */}
          {step === 0 && (
            <div className="card animate-fade-in">
              <div className="card__header"><div className="card__title">Choose a Resource</div></div>
              {resources.filter((r) => r.isActive).length === 0 ? (
                <div className="empty-state"><div className="empty-state__title">No resources available</div></div>
              ) : (
                <div className="grid-2">
                  {resources.filter((r) => r.isActive).map((res) => {
                    const t = res.reservationType;
                    return (
                      <div key={res.id}
                        className={`resource-card${selectedResource?.id === res.id ? ' selected' : ''}`}
                        onClick={() => setSelectedResource(res)}>
                        <div className="resource-card__icon" style={{ background: typeBg(t), color: typeColor(t) }}>
                          <span style={{ fontSize: '1.3rem' }}>{typeIcon(t)}</span>
                        </div>
                        <div className="resource-card__name">{res.name}</div>
                        <div className="resource-card__type">{typeLabel(t)}</div>
                        {res.totalCapacity ? (
                          <div style={{ marginTop: 8, fontSize: 'var(--font-size-xs)', color: 'var(--color-text-secondary)' }}>
                            Capacity: {res.totalCapacity}
                          </div>
                        ) : null}
                      </div>
                    );
                  })}
                </div>
              )}
              <div style={{ marginTop: 'var(--space-6)', display: 'flex', justifyContent: 'flex-end' }}>
                <button className="btn btn-primary" disabled={!selectedResource}
                  onClick={() => setStep(1)}>
                  Continue →
                </button>
              </div>
            </div>
          )}

          {/* Step 1: Check Availability */}
          {step === 1 && selectedResource && (
            <div className="card animate-fade-in">
              <div className="card__header">
                <div>
                  <div className="card__title">
                    {typeIcon(resType)} {selectedResource.name}
                  </div>
                  <div className="card__subtitle">{typeLabel(resType)} {selectedResource.totalCapacity ? `· Capacity: ${selectedResource.totalCapacity}` : ''}</div>
                </div>
                <button className="btn btn-ghost btn-sm" onClick={() => { setSelectedResource(null); setStep(0); }}>
                  ← Change
                </button>
              </div>

              {/* TIME_BASED */}
              {resType === 'TIME_BASED' && (
                <>
                  <div className="card__title" style={{ marginBottom: 'var(--space-3)', fontSize: 'var(--font-size-sm)' }}>
                    Select a Time Slot
                  </div>
                  <div className="availability-grid">
                    {timeSlots.map((slot) => {
                      const full = slot.currentBookings >= slot.maxParallelCapacity;
                      return (
                        <div key={slot.id}
                          className={`slot-card${selectedSlotId === slot.id ? ' selected' : ''}${full ? ' slot-card--unavailable' : ''}`}
                          onClick={() => !full && setSelectedSlotId(slot.id)}>
                          <div className="slot-card__time">{slot.startTime} – {slot.endTime}</div>
                          <div className="slot-card__cap">
                            {full ? 'Full' : `${slot.currentBookings}/${slot.maxParallelCapacity} booked`}
                          </div>
                        </div>
                      );
                    })}
                    {timeSlots.length === 0 && <div className="empty-state"><div className="empty-state__title">No slots configured</div></div>}
                  </div>
                </>
              )}

              {/* SEAT_BASED */}
              {resType === 'SEAT_BASED' && (
                <>
                  <div style={{ marginBottom: 'var(--space-4)', fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>
                    Select a Seat
                  </div>
                  <div className="seat-grid">
                    {seats.map((seat) => (
                      <button key={seat.id}
                        className={`seat-btn${selectedSeatId === seat.id ? ' selected' : ''}${!seat.isAvailable ? ' seat-btn--taken' : ''}`}
                        onClick={() => seat.isAvailable && setSelectedSeatId(seat.id)}>
                        {seat.seatIdentifier}
                      </button>
                    ))}
                    {seats.length === 0 && <div className="empty-state"><div className="empty-state__title">No available seats</div></div>}
                  </div>
                  <div style={{ display: 'flex', gap: 'var(--space-4)', marginTop: 'var(--space-4)', fontSize: 'var(--font-size-xs)' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ width: 14, height: 14, background: 'var(--color-accent)', borderRadius: 3, display: 'inline-block' }} /> Selected
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ width: 14, height: 14, background: 'var(--color-surface-2)', border: '1px solid var(--color-border)', borderRadius: 3, opacity: 0.5, display: 'inline-block' }} /> Taken
                    </span>
                  </div>
                </>
              )}

              {/* QUOTA_BASED */}
              {resType === 'QUOTA_BASED' && (
                <>
                  <div style={{ marginBottom: 'var(--space-4)', fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>Select Quota Category</div>
                  <div className="availability-grid">
                    {quotas.map((q) => {
                      const full = q.currentUsage >= q.maxAllocation;
                      return (
                        <div key={q.id}
                          className={`slot-card${selectedQuotaId === q.id ? ' selected' : ''}${full ? ' slot-card--unavailable' : ''}`}
                          onClick={() => !full && setSelectedQuotaId(q.id)}>
                          <div className="slot-card__time">{q.quotaName}</div>
                          <div className="slot-card__cap">
                            {full ? 'Full' : `${q.currentUsage}/${q.maxAllocation} used`}
                          </div>
                        </div>
                      );
                    })}
                    {quotas.length === 0 && <div className="empty-state"><div className="empty-state__title">No quota pools defined</div></div>}
                  </div>
                </>
              )}

              {/* RESOURCE_BASED */}
              {resType === 'RESOURCE_BASED' && (
                <div className="form-row cols-2">
                  <div className="form-group">
                    <label className="form-label">Start Date & Time <span>*</span></label>
                    <input type="datetime-local" className="form-control" value={startDate}
                      onChange={(e) => setStartDate(e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">End Date & Time <span>*</span></label>
                    <input type="datetime-local" className="form-control" value={endDate}
                      onChange={(e) => setEndDate(e.target.value)} />
                  </div>
                </div>
              )}

              {/* CAPACITY_BASED */}
              {resType === 'CAPACITY_BASED' && (
                <div className="form-group" style={{ maxWidth: 200 }}>
                  <label className="form-label">Quantity <span>*</span></label>
                  <input type="number" min={1} max={selectedResource.totalCapacity} className="form-control"
                    value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} />
                  <span className="form-hint">Max: {selectedResource.totalCapacity}</span>
                </div>
              )}

              <div style={{ marginTop: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', justifyContent: 'flex-end' }}>
                <button className="btn btn-secondary" onClick={() => setStep(0)}>← Back</button>
                <button className="btn btn-primary" onClick={handleCheckAvailability}
                  disabled={
                    (resType === 'TIME_BASED' && !selectedSlotId) ||
                    (resType === 'SEAT_BASED' && !selectedSeatId) ||
                    (resType === 'QUOTA_BASED' && !selectedQuotaId) ||
                    (resType === 'RESOURCE_BASED' && (!startDate || !endDate))
                  }>
                  Check Availability →
                </button>
              </div>
            </div>
          )}

          {/* Step 2: Review Availability & Hold */}
          {step === 2 && availResult && (
            <div className="card animate-fade-in">
              <div className="card__header">
                <div className="card__title">Availability Result</div>
              </div>
              <div className="confirm-panel">
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Resource</span>
                  <span className="confirm-panel__val">{selectedResource.name}</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Type</span>
                  <span className="confirm-panel__val">{typeIcon(resType)} {typeLabel(resType)}</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Available</span>
                  <span className="confirm-panel__val" style={{ color: availResult.available ? 'var(--color-success)' : 'var(--color-danger)' }}>
                    {availResult.available ? '✓ Yes' : '✗ No'}
                  </span>
                </div>
                {availResult.message && (
                  <div className="confirm-panel__row">
                    <span className="confirm-panel__key">Message</span>
                    <span className="confirm-panel__val">{availResult.message}</span>
                  </div>
                )}
              </div>

              {availResult.available && (
                <div className="alert-banner alert-banner--info">
                  <Clock size={16} />
                  <span>A temporary hold will be placed on this resource. You must confirm within the policy-defined window.</span>
                </div>
              )}

              <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'flex-end', marginTop: 'var(--space-4)' }}>
                <button className="btn btn-secondary" onClick={() => setStep(1)}>← Back</button>
                {availResult.available && (
                  <button className="btn btn-primary" onClick={handleHold} disabled={holding}>
                    {holding ? <><div className="spinner spinner-sm" /> Placing Hold…</> : 'Place Hold →'}
                  </button>
                )}
              </div>
            </div>
          )}

          {/* Step 3: Hold Placed - Confirm */}
          {step === 3 && reservation && (
            <div className="card animate-fade-in">
              <div style={{ textAlign: 'center', marginBottom: 'var(--space-6)' }}>
                <div style={{
                  width: 64, height: 64, background: 'var(--color-warning-light)', borderRadius: '50%',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto var(--space-3)',
                }}>
                  <Clock size={28} color="var(--color-warning)" />
                </div>
                <h3 style={{ fontSize: 'var(--font-size-xl)', fontWeight: 700 }}>Hold Placed!</h3>
                <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)', marginTop: 4 }}>
                  Confirm your booking before the hold expires
                </p>
                {timeLeft && (
                  <div className="countdown" style={{ margin: 'var(--space-3) auto', width: 'fit-content' }}>
                    <Clock size={14} /> Time remaining: {timeLeft}
                  </div>
                )}
              </div>

              <div className="confirm-panel">
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Reservation ID</span>
                  <span className="confirm-panel__val">#{reservation.id}</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Resource</span>
                  <span className="confirm-panel__val">{selectedResource.name}</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Type</span>
                  <span className="confirm-panel__val">{typeIcon(resType)} {typeLabel(resType)}</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">User</span>
                  <span className="confirm-panel__val">{user.userId}</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Status</span>
                  <span className="confirm-panel__val" style={{ color: 'var(--color-held)' }}>HELD</span>
                </div>
                <div className="confirm-panel__row">
                  <span className="confirm-panel__key">Hold Expires</span>
                  <span className="confirm-panel__val">{formatDateTime(reservation.holdExpiresAt)}</span>
                </div>
                {reservation.quantity && (
                  <div className="confirm-panel__row">
                    <span className="confirm-panel__key">Quantity</span>
                    <span className="confirm-panel__val">{reservation.quantity}</span>
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'flex-end', marginTop: 'var(--space-5)' }}>
                <button className="btn btn-danger btn-sm" onClick={() => navigate('/user/reservations')}>
                  Skip (Cancel Later)
                </button>
                <button className="btn btn-primary" onClick={handleConfirm} disabled={confirming}>
                  {confirming
                    ? <><div className="spinner spinner-sm" /> Confirming…</>
                    : <><CheckCircle2 size={15} /> Confirm Booking</>}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
